# Rewards Calculator

This project is intended to solve the problems outlined here: 
https://fetch-hiring.s3.us-east-1.amazonaws.com/points.pdf

# Requirements

There are two options to run the software. 

1) If you have java 11 and maven installed, you can run the service by running
    ```mvn clean install``` from the project directory, and then running ```java -jar target/rewards-0.0.1-SNAPSHOT.jar```

2) If you would instead prefer to run it via docker, this can be done by running 
```docker build . -t rewards```, and then running ```docker run -p 8080:8080 rewards```
   
Once your chosen deployment step is complete, you can test the service by running the following commands curl commands:

To Add Points:

curl --request POST \
--url http://localhost:8080/rewards/ \
--header 'Content-Type: application/json' \
--data '[{"payer": "DANNON", "points": 1000, "timestamp": "2020-11-02T14:00:00Z" }
,{ "payer": "UNILEVER", "points": 200, "timestamp": "2020-10-31T11:00:00Z" }
,{ "payer": "DANNON", "points": -200, "timestamp": "2020-10-31T15:00:00Z" }
,{ "payer": "MILLER COORS", "points": 10000, "timestamp": "2020-11-01T14:00:00Z" }
,{ "payer": "DANNON", "points": 300, "timestamp": "2020-10-31T10:00:00Z" }]'

The endpoint accepts a list of point objects.

To view the current balance:

curl --request GET \
--url http://localhost:8080/rewards

To spend points:  

curl --request POST \
--url http://localhost:8080/rewards/spend/ \
--header 'Content-Type: application/json' \
--data '{"points":5000}'