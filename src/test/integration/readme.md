# Slay the Spire Cards API Integration Tests

## Usage

1. Install dependencies:
```
$ pip3 install -r requirements.txt
```

2. Copy example-configuration.json to configuration.json making any needed changes.

3. Start an instance of the API:
```
$ gradle run
```

4. Run the integration tests:
```
$ python3 integration-tests.py -v -i configuration.json
```
