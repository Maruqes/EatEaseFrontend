.PHONY: clean build run build-linux run-linux build-mac run-mac

# Cleans the project
clean:
	@mvn clean

# Build target for all platforms
build:
	@mvn clean install

# Run target for all platforms
run:
	@mvn javafx:run

# Linux-specific build and run (if needed, same as default)
build-linux:
	@mvn clean install

run-linux:
	@mvn javafx:run

# macOS-specific build and run (if needed, same as default)
build-mac:
	@mvn clean install

run-mac:
	@mvn javafx:run