# bank app
This app is mini bank, where user can transfer money to another user.
There are 3 microservices, where forex service provides current currency exchange values, 
and conversion service convert amount from EUR to UAH, for example.
The third microservice is user service and it's a central part of the app, where available crud operations
with account. Also, user can transfer money, of course, if sum on a bank card lets to do it.
For example, user has 3000 UAH on his card and he wants to transfer 1500 UAH to another card, 
another user has EUR card. So, the microservices communicate with each other, and 1500 UAH converts in EUR 
by currency exchange value and sends to EUR card of another user in EUR currency.
User can authenticate with jwt token.
