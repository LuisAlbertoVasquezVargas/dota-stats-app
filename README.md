# dota-stats-app
A java app that grabs data from games and show summary stats

## Prerequisites

- **Docker:** Make sure Docker is installed and running on your system.
- **WSL:** Windows Subsystem for Linux is required if you're running this on Windows.

# Git clone
```bash
gh auth login
gh repo clone LuisAlbertoVasquezVargas/dota-stats-app
```

## Docker Instructions

### Build the Docker Image

Run the following command in the project directory to build the Docker image:

```bash
sudo docker build -t java21-hello-world .
```

### Run the Docker Container

After the image is built, start a container using:

```bash
sudo docker run --rm java21-hello-world
```