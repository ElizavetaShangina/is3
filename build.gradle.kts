plugins {
    id("java")
    id("war")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // Репозиторий JBoss нужен для WildFly/RESTEasy зависимостей
    maven { url = uri("https://repository.jboss.org/nexus/content/groups/public-jboss/") }
}

// Определяем версию Jackson один раз, чтобы она была везде одинаковой
val jacksonVersion = "2.15.2"

configurations.all {
    resolutionStrategy {
        // "Ядерное" решение: заставляем ВСЕ зависимости использовать именно эту версию Jackson.
        // Это предотвратит попытки скачать несуществующую 2.15.3-jakarta
        eachDependency {
            if (requested.group.startsWith("com.fasterxml.jackson")) {
                useVersion(jacksonVersion)
            }
        }
    }
}

dependencies {
    // --- DB & Pool ---
    implementation("org.postgresql:postgresql:42.5.0")
    implementation("org.apache.commons:commons-dbcp2:2.9.0")

    // --- Jakarta EE ---
    implementation("jakarta.platform:jakarta.jakartaee-api:10.0.0")
    compileOnly("jakarta.platform:jakarta.jakartaee-api:10.0.0")
    providedCompile("org.glassfish:jakarta.faces:4.0.0")

    // --- Utils ---
    implementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    implementation("com.opencsv:opencsv:5.7.1")

    // --- JSON (Jackson) ---
    // Явно подключаем платформу (BOM), хотя resolutionStrategy выше уже делает основную работу
    implementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonVersion"))

    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-annotations")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider")
    // Добавляем тот самый модуль, который вызывал ошибку, чтобы он точно подтянулся нужной версии
    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations")

    // --- RESTEasy ---
    // Исключаем встроенный Jackson, чтобы использовать наш
    implementation("org.jboss.resteasy:resteasy-jackson2-provider:6.0.1.Final") {
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "com.fasterxml.jackson.jaxrs")
        exclude(group = "com.fasterxml.jackson.module")
    }
    implementation("org.jboss.resteasy:resteasy-multipart-provider:6.0.1.Final")

    // --- JPA / EclipseLink ---
    implementation("org.eclipse.persistence:org.eclipse.persistence.jpa:3.0.2")

    // --- UI ---
    implementation("org.primefaces:primefaces:12.0.0:jakarta")

    // --- Tests ---
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // --- ЛР3: MinIO & Infinispan ---
    implementation("io.minio:minio:8.5.7") {
        // MinIO может тянуть свой Jackson, исключаем его, чтобы работала наша стратегия
        exclude(group = "com.fasterxml.jackson.core")
    }
    implementation("org.infinispan:infinispan-core:14.0.0.Final")
}

tasks.test {
    useJUnitPlatform()
}

tasks.war {
    archiveFileName.set("lab1.war")
    // Проверьте, что путь актуален
    destinationDirectory.set(file("C:\\wildfly26.1.3\\wildfly-preview-26.1.3.Final\\wildfly-preview-26.1.3.Final\\standalone\\deployments"))
}