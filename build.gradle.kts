plugins {
    id("java")
    id("war")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation ("org.postgresql:postgresql:42.5.0")
    implementation ("jakarta.platform:jakarta.jakartaee-api:10.0.0")
    implementation ("org.projectlombok:lombok:1.18.28")
    implementation ("org.jboss.resteasy:resteasy-jackson2-provider:6.0.1.Final")
    implementation ("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    annotationProcessor ("org.projectlombok:lombok:1.18.28")
    implementation ("org.eclipse.persistence:org.eclipse.persistence.jpa:3.0.2")
    implementation ("org.primefaces:primefaces:12.0.0:jakarta")
    providedCompile("org.glassfish:jakarta.faces:4.0.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    compileOnly ("jakarta.platform:jakarta.jakartaee-api:10.0.0")
}


tasks.test {
    useJUnitPlatform()
}

tasks.war {
    archiveFileName.set("lab1.war")

    destinationDirectory.set(file("C:/wildfly26.1.3/wildfly-preview-26.1.3.Final/wildfly-preview-26.1.3.Final/standalone/deployments"))
}

//"C:\\wildfly26.1.3\\wildfly-preview-26.1.3.Final\\wildfly-preview-26.1.3.Final\\bin"