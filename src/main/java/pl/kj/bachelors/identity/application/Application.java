package pl.kj.bachelors.identity.application;

import org.apache.catalina.connector.Connector;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import pl.kj.bachelors.identity.application.dto.response.PersonDto;
import pl.kj.bachelors.identity.application.dto.response.UploadedFileResponse;
import pl.kj.bachelors.identity.application.dto.response.health.HealthCheckResponse;
import pl.kj.bachelors.identity.application.dto.response.health.SingleCheckResponse;
import pl.kj.bachelors.identity.application.model.HealthCheckResult;
import pl.kj.bachelors.identity.application.model.SingleCheckResult;
import pl.kj.bachelors.identity.domain.model.Person;
import pl.kj.bachelors.identity.domain.model.UploadedFile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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
		mapper.addMappings(new PropertyMap<Person, PersonDto>() {
			@Override
			protected void configure() {
				using(ctx -> ((Person) ctx.getSource()).getFirstName() + " " + ((Person) ctx.getSource()).getLastName()).map(source, destination.getName());
				map().setAge(2021 - source.getYearOfBirth());
			}
		});

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

		return mapper;
	}
}