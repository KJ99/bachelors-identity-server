package pl.kj.bachelors.identity.application;

import org.apache.catalina.connector.Connector;
import org.hibernate.validator.HibernateValidator;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;
import pl.kj.bachelors.identity.application.dto.response.ProfileResponse;
import pl.kj.bachelors.identity.application.dto.response.UploadedFileResponse;
import pl.kj.bachelors.identity.application.dto.response.UserVerificationResponse;
import pl.kj.bachelors.identity.application.dto.response.health.HealthCheckResponse;
import pl.kj.bachelors.identity.application.dto.response.health.SingleCheckResponse;
import pl.kj.bachelors.identity.application.model.HealthCheckResult;
import pl.kj.bachelors.identity.application.model.SingleCheckResult;
import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = "pl.kj.bachelors")
@EnableJpaRepositories("pl.kj.bachelors.identity.infrastructure.repository")
@EntityScan("pl.kj.bachelors.identity.domain.model")
@Configuration
@EnableSwagger2
public class Application {
	@Value("${http.port}")
	private int httpPort;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addAdditionalTomcatConnectors(createStandardConnector());

		return tomcat;
	}

	private Connector createStandardConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setPort(this.httpPort);

		return connector;
	}

	@Bean
	public Docket swagger() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("pl.kj.bachelors"))
				.paths(PathSelectors.regex("/v1/.*"))
				.build();
	}

	@Bean
	public ModelMapper mapper() {
		ModelMapper mapper = new ModelMapper();

		mapper.addMappings(new PropertyMap<SingleCheckResult, SingleCheckResponse>() {
			@Override
			protected void configure() {
				using(ctx -> ((SingleCheckResult) ctx.getSource()).isActive() ? "On" : "Off")
						.map(source, destination.getStatus());
			}
		});

		mapper.addMappings(new PropertyMap<HealthCheckResult, HealthCheckResponse>() {
			@Override
			protected void configure() {
				using(ctx -> ((HealthCheckResult) ctx.getSource())
							.getResults()
							.stream()
							.map(item -> mapper().map(item, SingleCheckResponse.class))
							.collect(Collectors.toList())
				).map(source, destination.getResults());
			}
		});

		mapper.addMappings(new PropertyMap<UploadedFile, UploadedFileResponse>() {
			@Override
			protected void configure() {
				map().setFileName(source.getOriginalFileName());
			}
		});

		mapper.addMappings(new PropertyMap<UserVerification, UserVerificationResponse>() {
			@Override
			protected void configure() {
				map().setVerificationToken(source.getToken());
			}
		});

		mapper.addMappings(new PropertyMap<User, ProfileResponse>() {
			@Override
			protected void configure() {
				using(ctx -> {
					User src = (User) ctx.getSource();
					return String.format("%s %s", src.getFirstName(), src.getLastName());
				}).map(source, destination.getName());
				map().setId(source.getUid());
			}
		});

		return mapper;
	}

	@Bean
	public DataSource dataSource(Environment env){
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name", ""));
		dataSource.setUrl(env.getProperty("spring.datasource.url"));
		dataSource.setUsername(env.getProperty("spring.datasource.username"));
		dataSource.setPassword(env.getProperty("spring.datasource.password"));
		return dataSource;
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(Environment env) {
		LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
		em.setDataSource(dataSource(env));
		em.setPackagesToScan("pl.kj.bachelors.identity.domain.model");

		JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);

		return em;
	}

	@Bean
	public PlatformTransactionManager transactionManager(Environment env) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory(env).getObject());

		return transactionManager;
	}

	@Bean
	public Validator validator (AutowireCapableBeanFactory beanFactory) {

		ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
				.configure().constraintValidatorFactory(new SpringConstraintValidatorFactory(beanFactory))
				.buildValidatorFactory();

		return validatorFactory.getValidator();
	}
}