## Useful links
https://www.overleaf.com/project/60702650f977ba657f126342

## Docker
### Build JAR
```
docker-compose -f .\docker-compose-build.yml up --remove-orphans
```
### Deploy microservices
```
docker-compose -p ecommerce -f .\docker-compose.yml up -d --remove-orphans
```

## Simulate order routine
- Run the file `addFunds.py` to add funds (this action is needed just for the first run)
- Run the file `orderRoutine.py` to simulate order routine