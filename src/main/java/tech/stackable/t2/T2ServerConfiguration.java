package tech.stackable.t2;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("!cli")
@EnableScheduling
public class T2ServerConfiguration {
}
