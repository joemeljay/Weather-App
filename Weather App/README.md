# Weather App

A simple Android weather application built with Kotlin that follows the flowchart logic for weather data retrieval.

## Features

- **City Input**: Enter any city name to get current weather information
- **Location-based Weather**: Use device GPS to get weather for current location
- **Weather Display**: Shows temperature, weather condition, humidity, and wind speed
- **Error Handling**: Validates city input and handles network errors gracefully
- **Material Design**: Clean and modern UI using Material Design components

## Flowchart Implementation

The app follows this exact flow:

1. **Start & Open App**: Application launches
2. **Enter City / Get Location**: User can input city name or use GPS location
3. **City Input Valid?**: Validates city name format
   - If invalid: Shows "Enter Valid City" error
   - If valid: Proceeds to API call
4. **Send Request to Weather API**: Calls OpenWeatherMap API
5. **Data Received?**: Checks API response
   - If no data: Shows "No Data / Network Issue" error
   - If data received: Proceeds to parsing
6. **Parse Weather Data**: Processes JSON response
7. **Display Weather Info**: Shows temperature, condition, humidity, and wind
8. **End**: Process completes

## Setup

1. **Get API Key**: 
   - Sign up at [OpenWeatherMap](https://openweathermap.org/api)
   - Get your free API key
   - Replace `YOUR_API_KEY_HERE` in `MainActivity.kt` with your actual API key

2. **Build and Run**:
   - Open the project in Android Studio
   - Sync Gradle files
   - Run on an emulator or physical device

## Requirements

- Android Studio Arctic Fox or later
- Android SDK 21 (Android 5.0) or higher
- Internet connection for API calls
- Location permissions for GPS-based weather

## Dependencies

- Retrofit2 for network calls
- Gson for JSON parsing
- Play Services Location for GPS
- Material Design Components
- Coroutines for asynchronous operations

## Project Structure

```
app/
├── src/main/java/com/example/weatherapp/
│   ├── MainActivity.kt              # Main activity with flowchart logic
│   ├── LocationManager.kt           # Location permission helper
│   ├── models/
│   │   └── WeatherResponse.kt       # Weather data models
│   └── network/
│       ├── WeatherApiService.kt     # API interface
│       └── RetrofitClient.kt        # Retrofit setup
└── src/main/res/
    ├── layout/
    │   └── activity_main.xml        # Main UI layout
    ├── values/
    │   ├── strings.xml              # String resources
    │   ├── colors.xml               # Color resources
    │   └── themes.xml               # Theme configuration
    └── drawable/
        └── weather_card_bg.xml      # Weather card background
```

## API Response Format

The app expects the following weather data structure:

```json
{
  "coord": {"lon": -0.1257, "lat": 51.5085},
  "weather": [{"id": 801, "main": "Clouds", "description": "few clouds"}],
  "main": {
    "temp": 15.5,
    "humidity": 75,
    "pressure": 1012
  },
  "wind": {"speed": 3.5, "deg": 230},
  "name": "London"
}
```

## Error Handling

- **Invalid City**: Shows "Enter Valid City" for malformed city names
- **City Not Found**: Shows "Enter Valid City" when API returns 404
- **Network Issues**: Shows "No Data / Network Issue" for connection problems
- **Location Permission**: Shows error dialog when location permission is denied

## License

This project is open source and available under the MIT License.
