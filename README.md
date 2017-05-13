# Fred.moe
[fred.moe](https://fred.moe/) is a simplified file sharing service built with ease of installation in mind. Fred.moe comes with many features out of the box:

* Simple user interface
* HTTPs by default (via [Let's Encrypt](https://letsencrypt.org/))
* Self-updating [ClamAV](http://www.clamav.net/) scans your files daily and upon upload
* Everything is contained in a Docker image meaning minimal configuration


## Installation
### Step 1: Install Docker
if you are on a Linux system, install Docker with this command:
```
curl -sSL https://get.docker.com/ | sh
```

Otherwise take a look at [how to install Docker](https://www.docker.com/community-edition#/download)

### Step 2: Install Docker Compose
Please take a look at [the docker docs](https://docs.docker.com/compose/install/).

### Step 3: Configure the service
Create a new **permanent** directory for fred.moe. Create a file called `docker-compose.yml` and paste this into it:
```yaml
version: '2'

services:
  moe:
    image: frederikam/fred.moe:latest
    hostname: moe
    container_name: moe
    ports:
    - "80:80"
    - "443:443"
    - "8080:8080"
    environment:
    # This is required for automatic HTTPS! Setting this value
    # indicates that you agree to Let's Encrypt Subscriber Agreement.
    # No further action is required on your part
    - CADDY_EMAIL=
    volumes:
    # By default your data will be mounted to ./moedata . Feel free to change this value, but keep the `:/home/data` part.
    - ./moedata:/home/data
```

Remember to change the `CADDY_EMAIL` setting.

### Step 4: Deployment
To download and run the latest container, all you need to do is run one command:

```
docker-compose up moe -d
```

If you want to peek inside the container, you can run this command:

You are free to edit the `/home/public/` folder which contains all the static files.

```
docker exec -it moe bash
```

Run this command if you want to look at the logs:

```
docker logs -f moe
```

## Updating
This command will update your image, but will by default discard all data but your uploaded files:

```
docker pull frederikam/fred.moe && docker-compose up moe -d
```

## Contributing
Pull requests are welcome. Please follow the code style used here.

## Feature requests
Open a GitHub issue and I'll get to you soon, or just contact me directly. 
