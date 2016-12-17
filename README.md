# Fred.moe
[fred.moe](https://fred.moe/) is a bare-bones file hosting site I made and can be easily set up on other servers.

## Installation
1. Grab a release from [the releases](https://github.com/Frederikam/fred.moe/releases) or compile it yourself with `mvn package shade:shade`.
2. Install Java 8 if you haven't already. The JDK is not required for runtime.
3. Edit `config.json` to fit your particular setup.
4. Start the web server with `java -jar fred.moe-x.x.jar`.

You are free to edit the `public/` folder which contains all the static files.

This will make the server serve HTTP on port 8080. It is recommended to use [Caddy](https://caddyserver.com/) or some other web server to proxy the connection via port 80 and 443 as HTTPS. Here is an example caddyfile with HTTPS:
```
fred.moe               # Replace with your domain
proxy / localhost:8080 # Direct any requests from / to the local fred.moe server 
```

## Contributing
Pull requests are welcome. Please follow the code style used here.

## Feature requests
Open a GitHub issue and I'll get to you soon, or just contact me directly. 
