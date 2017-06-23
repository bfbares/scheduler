# Scheduler

Genetic algorithm to schedule an appointment in an car shop. 

## Installing

Install Java 8

Install Maven

Package with maven

```
mvn package
```

### Running in a Servlet container

You can run in a Servlet container that supports Websockets like Tomcat 8.

### Running in Docker

You can run the scheduler in Docker.

First you have to build the image with the Dockerfile in target

```
docker build -t scheduler .
```

Then run it

```
docker run -it --rm -p 80:8080 scheduler
```

## Built With

* [Spring](https://github.com/spring-projects)
* [jQuery](https://github.com/jquery/jquery)
* [moment](https://github.com/moment/moment)
* [FullCalendar](https://github.com/fullcalendar/fullcalendar)

## Author

**Borja Bares**

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details