### To build container:

```docker-compose```

### To bring all containers up

```docker-compose up -d```
This will bring the zookeeper and kafka container up

### Bring the app container up and run specific commands in your app container:

```docker-compose run app /bin/bash```

### To list all running containers

```docker ps```

### To exit app container

```exit```

### To bring all containers down

```docker-compose down```

### To run a specific main method class:

- Firstly you need to access to app container terminal ```docker-compose run app /bin/bash```
- Then run ```java -cp target/invensys-1.0-SNAPSHOT.jar [[class package].[class name]]```

### To run wrk performance test

1. Download wrk tool [here](https://medium.com/@felipedutratine/intelligent-benchmark-with-wrk-163986c1587f)
2. Download LuaRocks to install lua library: ```sudo apt install luarocks```
3. Download ```luasocket``` to for a precise time counter : ```luarocks install luasocket```
4. Change parameter that you want and run test at [wrk_test.sh](src/main/java/other/performance_test/script/wrk_test.sh)