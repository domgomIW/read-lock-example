# README

### Project
Example to reproduce redisson [issue](https://github.com/redisson/redisson/issues/1219)

### Installation requirements
- Docker
### Installation steps
- run Redis locally:
```bash
docker run --name some-redis -d -p 6379:6379 redis 
```

- run boot tests
```bash
./gradlew test --rerun-tasks
```


