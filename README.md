To run the application, follow these steps:

    Start the containers by running the command: docker-compose up
    Then, run the project by executing the RivertechApplication.kt class.


To run tests, right click test folder, and select run Tests in Rivertech
# API Endpoints

### GET /bet/history/{playerId}
### GET /leaderboard/winners
### GET /player/transactions/{playerId}
### POST /player/deposit/{playerId}

Request Body Example:

{
"amount": 1000
}
### POST /player/register

Request Body Example:

{
"name": "John",
"surname": "Wick",
"username": "john.wick"
}
### POST /bet/place

Request Body:

{
"playerId": 1,
"betAmount": 200.00,
"betNumber": 7,
"gameType": "ODDS_BASED"
}

