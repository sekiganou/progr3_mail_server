MAIN_CLASS=progr3.mail.server.app.Launcher

build:
	mvn clean compile

test: build
	mvn test

run: build
	mvn exec:java -Dexec.mainClass=$(MAIN_CLASS)

clean:
	mvn clean

.PHONY: build test run clean