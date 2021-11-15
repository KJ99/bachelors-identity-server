package pl.kj.bachelors.identity.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.apache.catalina.connector.Connector;
import org.hibernate.validator.HibernateValidator;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.validation.beanvalidation.SpringConstraintValidatorFactory;
import org.springframework.web.util.UriComponentsBuilder;
import pl.kj.bachelors.identity.application.config.AppConfig;
import pl.kj.bachelors.identity.application.config.HttpConfig;
import pl.kj.bachelors.identity.application.dto.response.ProfileResponse;
import pl.kj.bachelors.identity.application.dto.response.PublicProfileResponse;
import pl.kj.bachelors.identity.application.dto.response.UploadedFileResponse;
import pl.kj.bachelors.identity.application.dto.response.UserVerificationResponse;
import pl.kj.bachelors.identity.application.dto.response.health.HealthCheckResponse;
import pl.kj.bachelors.identity.application.dto.response.health.SingleCheckResponse;
import pl.kj.bachelors.identity.application.model.HealthCheckResult;
import pl.kj.bachelors.identity.application.model.SingleCheckResult;
import pl.kj.bachelors.identity.domain.config.ApiConfig;
import pl.kj.bachelors.identity.domain.config.JwtConfig;
import pl.kj.bachelors.identity.domain.model.entity.UploadedFile;
import pl.kj.bachelors.identity.domain.model.entity.User;
import pl.kj.bachelors.identity.domain.model.entity.UserVerification;
import pl.kj.bachelors.identity.domain.model.update.UserUpdateModel;

import javax.sql.DataSource;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.stream.Collectors;

@SpringBootApplication(scanBasePackages = "pl.kj.bachelors")
@EnableJpaRepositories("pl.kj.bachelors.identity.infrastructure.repository")
@EntityScan("pl.kj.bachelors.identity.domain.model")
@Configuration
public class Application {
	@Autowired
	private HttpConfig httpConfig;

	 @Autowired
	private ApiConfig apiConfig;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private JwtConfig jwtConfig;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public OpenAPI openApi() {
		Components swaggerComponents = new Components();
		SecurityScheme securityScheme = new SecurityScheme()
				.type(SecurityScheme.Type.HTTP)
				.scheme(this.jwtConfig.getType())
				.bearerFormat("JWT");

		swaggerComponents
				.addSecuritySchemes("JWT", securityScheme);

		Info apiInfo = new Info()
				.title(this.appConfig.getName())
				.description(this.appConfig.getDescription())
				.version(this.appConfig.getVersion());

		return new OpenAPI()
				.components(swaggerComponents)
				.info(apiInfo);
	}

	@Bean
	public ModelResolver modelResolver(ObjectMapper objectMapper) {
		return new ModelResolver(objectMapper);
	}

	@Bean
	@Profile({"!prod"})
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addAdditionalTomcatConnectors(createStandardConnector());

		return tomcat;
	}

	private Connector createStandardConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setPort(this.httpConfig.getPort());

		return connector;
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
				map().setId(source.getUid());
				using(ctx -> {
					User src = (User) ctx.getSource();
					UriComponentsBuilder uriBuilder = UriComponentsBuilder
							.newInstance()
							.host(apiConfig.getHost())
							.scheme("https");

					return src.getPicture() != null
							? uriBuilder
								.path("/v1/resources/{id}/download")
								.buildAndExpand(src.getPicture().getId())
								.toUriString()
							: null;
				}).map(source, destination.getPictureUrl());
			}
		});

		mapper.addMappings(new PropertyMap<User, PublicProfileResponse>() {
			@Override
			protected void configure() {
				map().setId(source.getUid());
				using(ctx -> {
					User src = (User) ctx.getSource();
					UriComponentsBuilder uriBuilder = UriComponentsBuilder
							.newInstance()
							.host(apiConfig.getHost())
							.scheme("https");

					return src.getPicture() != null
							? uriBuilder
							.path("/v1/resources/{id}/download")
							.buildAndExpand(src.getPicture().getId())
							.toUriString()
							: null;
				}).map(source, destination.getPictureUrl());
			}
		});

		mapper.addMappings(new PropertyMap<User, UserUpdateModel>() {
			@Override
			protected void configure() {
				map().setUsername(source.getUserName());
				using(ctx -> {
					User src = (User) ctx.getSource();
					return src.getPicture() != null ? src.getPicture().getId() : null;
				}).map(source, destination.getPictureId());
			}
		});

		mapper.addMappings(new PropertyMap<UserUpdateModel, User>() {
			@Override
			protected void configure() {
				map().setUserName(source.getUsername());
				using(ctx -> {
					UserUpdateModel src = (UserUpdateModel) ctx.getSource();
					UploadedFile file = new UploadedFile();
					file.setId(src.getPictureId());
					return src.getPictureId() != null ? file : null;
				}).map(source, destination.getPicture());
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