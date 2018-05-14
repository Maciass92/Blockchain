# Blockchain info

Cryptocurriencies related web app. To be more specific turtle coin, but possibly could be used for others. The app connects to coin's global network & several coin pools to gather hashrate value. Used for mining performance imporvements.

## Getting Started

git clone https://github.com/Maciass92/Blockchain-Info.git

cd Blockchain-Info

./mvnw spring-boot:run

Prior to running you need to setup a profile. The app can run either with in memory database or a Postgre DB. To change the profile go to: 

```Blockchain-Info/src/main/resources/application.properties```

and change the value of active profile to either:

```spring.profiles.active=postgre``` or
```spring.profiles.active=hsql```


## Tools used

* [Spring 5.0](https://spring.io/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [Hibernate](http://hibernate.org/) - ORM tool
* [Postgre database](https://www.postgresql.org/)
* [Thymeleaf](https://www.thymeleaf.org/) - Template engine
* [Project Lombok](https://projectlombok.org/) - Boilerplate 
code generator
* [Jackson] (https://github.com/FasterXML/jackson) - JSON parser for Java


### Known issues

Calling to pools' apis is done in a pitiful way. Currently working on async calls.


## Authors

Maciej Komorowski


