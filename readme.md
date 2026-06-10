[![Build Status](https://travis-ci.org/RutledgePaulV/NeoMasDewo.svg?branch=develop)](https://travis-ci.org/RutledgePaulV/NeoMasDewo)
[![Coverage Status](https://coveralls.io/repos/github/RutledgePaulV/NeoMasDewo/badge.svg?branch=develop)](https://coveralls.io/github/RutledgePaulV/NeoMasDewo?branch=develop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.rutledgepaulv/NeoMasDewo/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.rutledgepaulv/NeoMasDewo)


# MasDewo Inventory System (Java Edition)

### Getting Started
1. **Clone the repo:** `git clone <url>`
2. **Setup DB:** Run the SQL script in `/database/schema.sql` on your local MySQL.
3. **Configure DB:** Update `DBConnection.java` with your local root password.
4. **Build:** Run `mvn clean install` to download dependencies.

### Project Structure
- `models`: Plain Java objects (POJOs). **System Lead** handles this.
- `dao`: Raw SQL logic. **DAO Lead (Me)** handles this.
- `services`: Business logic/Transactions. **Logic Lead** handles this.
- `ui`: FXML and Controllers. **UI Lead** handles this.

### Rules
- **No Frameworks.** Vanilla JDBC only.
- **Git:** `git pull` before you start. `git push` once your feature is tested.
- **Dates:** Use `LocalDateTime` ONLY.


Release Versions
```xml
<dependencies>
    <dependency>
        <groupId>com.rplbo.app</groupId>
        <artifactId>NeoMasDewo</artifactId>
        <version><!-- Not yet released --></version>
    </dependency>
</dependencies>
```

Snapshot Versions
```xml
<dependencies>
    <dependency>
        <groupId>com.rplbo.app</groupId>
        <artifactId>NeoMasDewo</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <id>ossrh</id>
        <name>Repository for snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```


This project is licensed under [MIT license](http://opensource.org/licenses/MIT).
