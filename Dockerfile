FROM openjdk:21-slim

WORKDIR /java-app

# Copy your Java source file and .env file into the container
COPY GraphqlMatchStats.java .
COPY .env .

# Install wget and download required JARs into a libs directory
RUN apt-get update && apt-get install -y wget && \
    mkdir libs && \
    wget -O libs/jackson-core.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.15.2/jackson-core-2.15.2.jar && \
    wget -O libs/jackson-databind.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.15.2/jackson-databind-2.15.2.jar && \
    wget -O libs/jackson-annotations.jar https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.15.2/jackson-annotations-2.15.2.jar && \
    wget -O libs/dotenv-java.jar https://repo1.maven.org/maven2/io/github/cdimascio/dotenv-java/2.2.0/dotenv-java-2.2.0.jar

# Compile the Java source file using the downloaded JARs
RUN javac -cp "libs/*" GraphqlMatchStats.java

# Run the program with the current directory and libs on the classpath
CMD ["java", "-cp", ".:libs/*", "GraphqlMatchStats"]
