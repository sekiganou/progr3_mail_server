#!/usr/bin/env bash

# nix-shell --command 'mvn clean compile exec:java -Dexec.mainClass="progr3.mail.server.app.Launcher"'

# Run inside a nix-shell
mvn clean compile exec:java -Dexec.mainClass="progr3.mail.server.app.Launcher"
