# Payment Gateway Swing Desktop App

A self-contained Java Swing project demonstrating a payment gateway with CARD/UPI strategies, asynchronous simulated processing, in-memory repositories, idempotency, transaction history, and status refresh. It requires JDK 11+ and no external libraries.

## Run on Windows
1. Extract this folder.
2. Open CMD in the `PaymentGatewaySwing` folder.
3. Run `run.bat`.

Or compile manually in CMD:

```cmd
if not exist out mkdir out
for /r src %f in (*.java) do javac -encoding UTF-8 -d out -sourcepath src "%f"
java -cp out gui.PaymentGatewayApp
```

For a `.bat` file, use `%%f` instead of `%f` in the `for` loop.

## Features
- Create a payment with amount, CARD/UPI, and an idempotency key.
- Generate a fresh key with one click.
- Duplicate keys return the original payment and do not start processing again.
- Async simulated status update (about 1.6 seconds, approximately 80% success rate).
- Automatic status refresh and manual refresh.
- Transaction history and selected-payment detail dialog.
- All data is in memory and is lost when the app closes. No real money is transferred.

This is an educational local demo, not a production-ready distributed gateway. It is self-contained so it compiles without needing the original repository checkout. It follows the same general service/model/processor/repository pattern, but does not automatically merge with or modify the GitHub project.
