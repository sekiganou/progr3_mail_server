MAIN_CLASS=progr3.mail.server.app.Launcher

build:
	mvn clean compile

test: build
	mvn test

run:
	@mkdir -p logs
	mvn exec:java -Dexec.mainClass=$(MAIN_CLASS) \
		> logs/app.log 2>&1 &


clean:
	mvn clean

.PHONY: build test run clean