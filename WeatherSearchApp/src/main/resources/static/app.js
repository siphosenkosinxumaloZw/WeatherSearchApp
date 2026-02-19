const { useState, useEffect } = React;

const API_BASE = '/api';

const WeatherApp = () => {
    const [locations, setLocations] = useState([]);
    const [selectedLocation, setSelectedLocation] = useState(null);
    const [weatherData, setWeatherData] = useState(null);
    const [forecastData, setForecastData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [showAddModal, setShowAddModal] = useState(false);
    const [preferences, setPreferences] = useState({
        temperatureUnit: 'celsius',
        windSpeedUnit: 'kmh',
        pressureUnit: 'hPa',
        refreshInterval: 30,
        autoRefresh: false
    });

    useEffect(() => {
        fetchLocations();
        fetchPreferences();
        const interval = setInterval(() => {
            if (preferences.autoRefresh) {
                fetchLocations();
                if (selectedLocation) {
                    fetchWeatherData(selectedLocation.id);
                }
            }
        }, preferences.refreshInterval * 60 * 1000);
        return () => clearInterval(interval);
    }, [preferences.autoRefresh, preferences.refreshInterval, selectedLocation]);

    const fetchLocations = async () => {
        try {
            const response = await fetch(`${API_BASE}/locations`);
            if (response.ok) {
                const data = await response.json();
                setLocations(data);
            }
        } catch (err) {
            setError('Failed to fetch locations');
        }
    };

    const fetchPreferences = async () => {
        try {
            const response = await fetch(`${API_BASE}/preferences`);
            if (response.ok) {
                const data = await response.json();
                setPreferences(data);
            }
        } catch (err) {
            console.error('Failed to fetch preferences');
        }
    };

    const fetchWeatherData = async (locationId) => {
        setLoading(true);
        setError(null);
        try {
            const [weatherResponse, forecastResponse] = await Promise.all([
                fetch(`${API_BASE}/weather/current/${locationId}`),
                fetch(`${API_BASE}/weather/forecast/${locationId}`)
            ]);

            if (weatherResponse.ok) {
                const weather = await weatherResponse.json();
                setWeatherData(weather);
            } else {
                await fetch(`${API_BASE}/weather/sync/${locationId}`, { method: 'POST' });
                const retryResponse = await fetch(`${API_BASE}/weather/current/${locationId}`);
                if (retryResponse.ok) {
                    const weather = await retryResponse.json();
                    setWeatherData(weather);
                }
            }

            if (forecastResponse.ok) {
                const forecast = await forecastResponse.json();
                setForecastData(forecast);
            }
        } catch (err) {
            setError('Failed to fetch weather data');
        } finally {
            setLoading(false);
        }
    };

    const addLocation = async (locationData) => {
        try {
            const response = await fetch(`${API_BASE}/locations`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(locationData)
            });
            if (response.ok) {
                fetchLocations();
                setShowAddModal(false);
            } else {
                setError('Failed to add location');
            }
        } catch (err) {
            setError('Failed to add location');
        }
    };

    const deleteLocation = async (locationId) => {
        try {
            const response = await fetch(`${API_BASE}/locations/${locationId}`, {
                method: 'DELETE'
            });
            if (response.ok) {
                fetchLocations();
                if (selectedLocation?.id === locationId) {
                    setSelectedLocation(null);
                    setWeatherData(null);
                    setForecastData(null);
                }
            }
        } catch (err) {
            setError('Failed to delete location');
        }
    };

    const toggleFavorite = async (locationId) => {
        try {
            const response = await fetch(`${API_BASE}/locations/${locationId}/toggle-favorite`, {
                method: 'POST'
            });
            if (response.ok) {
                fetchLocations();
            }
        } catch (err) {
            setError('Failed to toggle favorite');
        }
    };

    const syncWeather = async (locationId) => {
        setLoading(true);
        try {
            const response = await fetch(`${API_BASE}/weather/sync/${locationId}`, {
                method: 'POST'
            });
            if (response.ok) {
                fetchWeatherData(locationId);
                fetchLocations();
            }
        } catch (err) {
            setError('Failed to sync weather');
        } finally {
            setLoading(false);
        }
    };

    const formatTemperature = (temp) => {
        if (preferences.temperatureUnit === 'fahrenheit') {
            return Math.round((temp * 9/5) + 32) + '°F';
        }
        return Math.round(temp) + '°C';
    };

    const formatWindSpeed = (speed) => {
        if (preferences.windSpeedUnit === 'mph') {
            return Math.round(speed * 0.621371) + ' mph';
        }
        return Math.round(speed * 3.6) + ' km/h';
    };

    const getWeatherIcon = (iconCode) => {
        return `https://openweathermap.org/img/wn/${iconCode}@2x.png`;
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
            <div className="container mx-auto px-4 py-8">
                <header className="mb-8">
                    <h1 className="text-4xl font-bold text-gray-800 mb-2">
                        <i className="fas fa-cloud-sun mr-2"></i>
                        Weather Search App
                    </h1>
                    <p className="text-gray-600">Track weather for your favorite locations</p>
                </header>

                {error && (
                    <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                        {error}
                        <button onClick={() => setError(null)} className="float-right">
                            <i className="fas fa-times"></i>
                        </button>
                    </div>
                )}

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    <div className="lg:col-span-1">
                        <div className="bg-white rounded-lg shadow-md p-6 mb-4">
                            <div className="flex justify-between items-center mb-4">
                                <h2 className="text-xl font-semibold">Locations</h2>
                                <button
                                    onClick={() => setShowAddModal(true)}
                                    className="bg-blue-500 text-white px-3 py-1 rounded hover:bg-blue-600"
                                >
                                    <i className="fas fa-plus mr-1"></i> Add
                                </button>
                            </div>
                            <div className="space-y-2 max-h-96 overflow-y-auto">
                                {locations.map(location => (
                                    <div
                                        key={location.id}
                                        className={`p-3 border rounded cursor-pointer transition-colors ${
                                            selectedLocation?.id === location.id
                                                ? 'border-blue-500 bg-blue-50'
                                                : 'border-gray-200 hover:bg-gray-50'
                                        }`}
                                        onClick={() => {
                                            setSelectedLocation(location);
                                            fetchWeatherData(location.id);
                                        }}
                                    >
                                        <div className="flex justify-between items-center">
                                            <div>
                                                <div className="font-medium">
                                                    {location.displayName}
                                                    {location.isFavorite && (
                                                        <i className="fas fa-star text-yellow-500 ml-2"></i>
                                                    )}
                                                </div>
                                                <div className="text-sm text-gray-500">
                                                    {location.lastSyncAt
                                                        ? `Last sync: ${new Date(location.lastSyncAt).toLocaleTimeString()}`
                                                        : 'Not synced yet'
                                                    }
                                                </div>
                                            </div>
                                            <div className="flex space-x-1">
                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        toggleFavorite(location.id);
                                                    }}
                                                    className="p-1 hover:bg-gray-200 rounded"
                                                >
                                                    <i className={`fas fa-star ${location.isFavorite ? 'text-yellow-500' : 'text-gray-300'}`}></i>
                                                </button>
                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        syncWeather(location.id);
                                                    }}
                                                    className="p-1 hover:bg-gray-200 rounded"
                                                    disabled={loading}
                                                >
                                                    <i className="fas fa-sync-alt"></i>
                                                </button>
                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        if (confirm('Delete this location?')) {
                                                            deleteLocation(location.id);
                                                        }
                                                    }}
                                                    className="p-1 hover:bg-gray-200 rounded"
                                                >
                                                    <i className="fas fa-trash text-red-500"></i>
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div className="bg-white rounded-lg shadow-md p-6">
                            <h3 className="text-lg font-semibold mb-3">Settings</h3>
                            <div className="space-y-3">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Temperature Unit
                                    </label>
                                    <select
                                        value={preferences.temperatureUnit}
                                        onChange={(e) => setPreferences({...preferences, temperatureUnit: e.target.value})}
                                        className="w-full p-2 border rounded"
                                    >
                                        <option value="celsius">Celsius</option>
                                        <option value="fahrenheit">Fahrenheit</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">
                                        Auto Refresh
                                    </label>
                                    <label className="flex items-center">
                                        <input
                                            type="checkbox"
                                            checked={preferences.autoRefresh}
                                            onChange={(e) => setPreferences({...preferences, autoRefresh: e.target.checked})}
                                            className="mr-2"
                                        />
                                        Enable auto refresh
                                    </label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="lg:col-span-2">
                        {selectedLocation && weatherData ? (
                            <div className="space-y-6">
                                <div className="bg-white rounded-lg shadow-md p-6">
                                    <div className="flex justify-between items-start mb-4">
                                        <div>
                                            <h2 className="text-2xl font-bold">{selectedLocation.displayName}</h2>
                                            <p className="text-gray-500">
                                                Last updated: {new Date(weatherData.timestamp).toLocaleString()}
                                            </p>
                                        </div>
                                        <button
                                            onClick={() => syncWeather(selectedLocation.id)}
                                            disabled={loading}
                                            className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 disabled:bg-gray-400"
                                        >
                                            <i className="fas fa-sync-alt mr-2"></i>
                                            {loading ? 'Syncing...' : 'Refresh'}
                                        </button>
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                        <div className="text-center">
                                            {weatherData.weatherIcon && (
                                                <img
                                                    src={getWeatherIcon(weatherData.weatherIcon)}
                                                    alt={weatherData.weatherDescription}
                                                    className="w-24 h-24 mx-auto mb-2"
                                                />
                                            )}
                                            <div className="text-4xl font-bold mb-2">
                                                {formatTemperature(weatherData.temperature)}
                                            </div>
                                            <div className="text-gray-600 capitalize">
                                                {weatherData.weatherDescription}
                                            </div>
                                        </div>

                                        <div className="space-y-3">
                                            <div className="flex justify-between">
                                                <span className="text-gray-600">Humidity:</span>
                                                <span className="font-medium">{weatherData.humidity}%</span>
                                            </div>
                                            <div className="flex justify-between">
                                                <span className="text-gray-600">Pressure:</span>
                                                <span className="font-medium">{weatherData.pressure} hPa</span>
                                            </div>
                                            {weatherData.windSpeed && (
                                                <div className="flex justify-between">
                                                    <span className="text-gray-600">Wind Speed:</span>
                                                    <span className="font-medium">{formatWindSpeed(weatherData.windSpeed)}</span>
                                                </div>
                                            )}
                                            {weatherData.visibility && (
                                                <div className="flex justify-between">
                                                    <span className="text-gray-600">Visibility:</span>
                                                    <span className="font-medium">{(weatherData.visibility / 1000).toFixed(1)} km</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>

                                {forecastData && (
                                    <div className="bg-white rounded-lg shadow-md p-6">
                                        <h3 className="text-xl font-semibold mb-4">5-Day Forecast</h3>
                                        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
                                            {forecastData.list.slice(0, 5).map((item, index) => (
                                                <div key={index} className="text-center p-3 border rounded">
                                                    <div className="font-medium mb-2">
                                                        {new Date(item.dt * 1000).toLocaleDateString('en', { weekday: 'short' })}
                                                    </div>
                                                    {item.weather && item.weather[0] && (
                                                        <img
                                                            src={getWeatherIcon(item.weather[0].icon)}
                                                            alt={item.weather[0].description}
                                                            className="w-12 h-12 mx-auto mb-2"
                                                        />
                                                    )}
                                                    <div className="text-sm">
                                                        {formatTemperature(item.main.temp)}
                                                    </div>
                                                    <div className="text-xs text-gray-500">
                                                        {item.main.humidity}%
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="bg-white rounded-lg shadow-md p-12 text-center">
                                <i className="fas fa-cloud-sun-rain text-6xl text-gray-300 mb-4"></i>
                                <h3 className="text-xl font-semibold text-gray-600 mb-2">
                                    Select a location to view weather
                                </h3>
                                <p className="text-gray-500">
                                    Choose a location from the list or add a new one to get started
                                </p>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {showAddModal && (
                <AddLocationModal
                    onClose={() => setShowAddModal(false)}
                    onAdd={addLocation}
                />
            )}
        </div>
    );
};

const AddLocationModal = ({ onClose, onAdd }) => {
    const [cityName, setCityName] = useState('');
    const [countryCode, setCountryCode] = useState('');
    const [latitude, setLatitude] = useState('');
    const [longitude, setLongitude] = useState('');
    const [displayName, setDisplayName] = useState('');
    const [isFavorite, setIsFavorite] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!cityName || !countryCode) return;

        setLoading(true);
        try {
            const locationData = {
                cityName,
                countryCode,
                ...(latitude && { latitude: parseFloat(latitude) }),
                ...(longitude && { longitude: parseFloat(longitude) }),
                ...(displayName && { displayName }),
                isFavorite
            };
            await onAdd(locationData);
            setCityName('');
            setCountryCode('');
            setLatitude('');
            setLongitude('');
            setDisplayName('');
            setIsFavorite(false);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md max-h-screen overflow-y-auto">
                <h3 className="text-xl font-semibold mb-4">Add New Location</h3>
                <form onSubmit={handleSubmit}>
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            City Name <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="text"
                            value={cityName}
                            onChange={(e) => setCityName(e.target.value)}
                            className="w-full p-2 border rounded"
                            placeholder="e.g., London"
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Country Code <span className="text-red-500">*</span>
                        </label>
                        <input
                            type="text"
                            value={countryCode}
                            onChange={(e) => setCountryCode(e.target.value.toUpperCase())}
                            className="w-full p-2 border rounded"
                            placeholder="e.g., GB"
                            maxLength={2}
                            required
                        />
                    </div>
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Latitude (Optional)
                        </label>
                        <input
                            type="number"
                            step="any"
                            value={latitude}
                            onChange={(e) => setLatitude(e.target.value)}
                            className="w-full p-2 border rounded"
                            placeholder="e.g., 51.5074"
                        />
                        <p className="text-xs text-gray-500 mt-1">If not provided, will be fetched from API</p>
                    </div>
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Longitude (Optional)
                        </label>
                        <input
                            type="number"
                            step="any"
                            value={longitude}
                            onChange={(e) => setLongitude(e.target.value)}
                            className="w-full p-2 border rounded"
                            placeholder="e.g., -0.1278"
                        />
                        <p className="text-xs text-gray-500 mt-1">If not provided, will be fetched from API</p>
                    </div>
                    <div className="mb-4">
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            Display Name (Optional)
                        </label>
                        <input
                            type="text"
                            value={displayName}
                            onChange={(e) => setDisplayName(e.target.value)}
                            className="w-full p-2 border rounded"
                            placeholder="e.g., London, United Kingdom"
                        />
                        <p className="text-xs text-gray-500 mt-1">If not provided, will be auto-generated</p>
                    </div>
                    <div className="mb-4">
                        <label className="flex items-center">
                            <input
                                type="checkbox"
                                checked={isFavorite}
                                onChange={(e) => setIsFavorite(e.target.checked)}
                                className="mr-2"
                            />
                            <span className="text-sm font-medium text-gray-700">Mark as Favorite</span>
                        </label>
                    </div>
                    <div className="flex justify-end space-x-2">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50"
                        >
                            Cancel
                        </button>
                        <button
                            type="submit"
                            disabled={loading}
                            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:bg-gray-400"
                        >
                            {loading ? 'Adding...' : 'Add Location'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

ReactDOM.render(<WeatherApp />, document.getElementById('root'));
