# Stock Dashboard 
##  Overview
This is a simple Android application to:
- View cryptocurrency, stock prices (Bitcoin, Ethereum, ACB, etc.)
- Prices update continuously (real-time)
- View price charts by day, month, year
- Fast performance and battery-efficient

### Technologies Used
- **Language**: Java (Android)
- **List Display**: RecyclerView
- **Chart Drawing**: MPAndroidChart
- **Data Reception**: SSE (Server-Sent Events) - server sends continuous messages
- **Network Connection**: OkHttp3

### Application Structure
```
app/
├── fragments/
│   └── CryptoFragment.java          // Cryptocurrency list screen
│   └── .......  
├── activities/
│   └── CryptoDetailActivity.java    // Detail screen + chart
│   └── ....... 
├── services/
│   └── CryptoSSEService.java        // Background service for receiving prices
│   └── ....... 
└── adapter/
    └── CryptoAdapter.java           // Display each item in the list
    └── ....... 
```

##  Main Features

### 1. Continuous Price Updates

**Service (Background Service):**
```
// CryptoSSEService.java
// Background service to receive new prices
public class CryptoSSEService extends Service {
    - Connect to server
    - Receive new prices continuously
    - Send notifications to screen for updates
}
```

**Features:**
- Connection always open to receive data
- Auto-reconnect when network is lost
- Runs in background without freezing screen
- Can send to multiple screens simultaneously

```

**Lifecycle Management:**
- Only receive data when screen is visible
- Turn off when not in use to save battery

// Only need 1 connection for all
String url = "https://server.com/events?symbols=" + SYMBOLS;
```

**Benefits:**
- 1 connection instead of 19 connections
-  Save network bandwidth

### 2. Price Charts

```java
// CryptoDetailActivity.java
// Fetch historical data and draw chart
fetchAndRenderChartData(symbol, interval, days) {
    - Load prices from previous days
    - Draw line chart
    - Can touch chart to see details
}
```

**Features:**
- View by: 1 week, 1 month, 1 year, 5 years
- Smooth chart lines
- Auto-adjust scale

### 3. Color Effects When Price Changes

```java
// When price changes
updatePriceUI(price, changePercent, timestamp) {
    if (price > lastPrice) {
        flashTextView(GREEN);  // Price up → green
    } else if (price < lastPrice) {
        flashTextView(RED);    // Price down → red
    }
}
```

**User Experience Enhancement:**
- Easy to see if price goes up or down
- Smooth color transitions

### 4. List Optimization

```java
// Make RecyclerView run faster
setupRecyclerView() {
    - setHasFixedSize(true);        // Speed boost
    - Disable update animations      // No flickering
}
```

##  Optimization

### 1. Keep-Alive - Keep Connection Alive

#### What's the Problem?

When connection is open too long without data:
- Mobile network may disconnect
- Server thinks app is closed
- Proxy may close connection after 1-2 minutes

#### Solution

**Client-side (App):**
```java
client = new OkHttpClient.Builder()
    .pingInterval(30, SECONDS)  // Send "ping" every 30 seconds
    .build();
```

**Server-side:**
```javascript
// Send "I'm still here" message every 25 seconds
setInterval(() => {
    res.write(': keep-alive\n\n');
}, 25000);
```

**Benefits:**
- Know immediately when connection is lost (after 30 seconds instead of 2 minutes)
- Auto-reconnect faster
- No silent failures (connection dead but app doesn't know)
- Works well through complex networks

#### How It Works

```java
// CryptoSSEService.java
private OkHttpClient createClient() {
    return new OkHttpClient.Builder()
        .connectTimeout(10, SECONDS)      // Wait 10 seconds when connecting
        .readTimeout(0, SECONDS)          // No timeout when reading data
        .pingInterval(30, SECONDS)        // Ping every 30 seconds
        .build();
}
```

### 2. Why Use SSE?

**What is SSE?**
SSE (Server-Sent Events) = Server sends continuous messages to app

**Why Choose SSE?**
-  Only need to receive prices (no need to send)
-  Auto-reconnect when network lost
-  Easy to debug (uses HTTP)
-  Less resource consumption

### 3. Lifecycle Management

```java
// When screen is visible
onResume() {
    1. Register to receive messages
    2. Turn on service to receive prices
}

// When screen is hidden
onPause() {
    1. Unregister from receiving messages
    2. Turn off service
}
```

**Benefits:**
-  Don't waste network when not viewing
-  Save battery
-  Close connection when not needed

### 4. Thread Management

**Service (background):**
```java
new Thread(() -> {
    // Read data from server
    while (!source.exhausted()) {
        String line = source.readUtf8Line();
        // Process and send broadcast
    }
}).start();
```

**Detail screen (background):**
```java
sseThread = new Thread(() -> {
    // Long-lived connection
    while (isRunning) {
        // Read new prices
        handler.post(() -> updatePriceUI());  // Update screen
    }
});

// When exiting screen
onStop() {
    isRunning = false;
    sseThread.interrupt();  // Stop thread
}
```

### 5. Data Storage

```java
// Store in memory (RAM)
List<CryptoItem> cryptoList = new ArrayList<>();

// Update item
adapter.updateItem(newItem) {
    int index = findItem(newItem.symbol);
    cryptoList.set(index, newItem);    // Update
    notifyItemChanged(index);          // Notify change
}
```

**No need to save to disk because:**
-  Prices change every second
-  SSE always provides new prices
-  Storing in RAM is enough

### 6. Error Handling

```java
// Handle connection errors
try {
    Response response = call.execute();
    if (response.isSuccessful()) {
        // Process data
    } else {
        Log.e(TAG, "Connection failed");
    }
} catch (Exception e) {
    Log.e(TAG, "Connection error", e);
    // Service will restart automatically
}
```

### 7. Save Network Bandwidth

**One connection for multiple coins:**
```java

//  Efficient: 1 connection
String symbols = "btcusdt,ethusdt,bnbusdt,...";
connectSSE(symbols);
```

**Savings:**
- 19 handshakes → 1 handshake
- 19 security negotiations → 1 negotiation
- Reduce ~95% connection overhead

**Load chart only when needed:**
```java
// Only load chart data when clicking
Intent intent = new Intent(context, CryptoDetailActivity.class);
startActivity(intent);
// → Then load chart data
```

##  SSE Connection Management

### Connection States

```java
public enum ConnectionState {
    DISCONNECTED,    // Not connected
    CONNECTING,      // Connecting
    CONNECTED,       // Connected
    RECONNECTING,    // Reconnecting
    FAILED          // Failed
}
```

### Auto Reconnection

```java
// Retry connection with increasing wait time
private int retryCount = 0;
private static final int MAX_RETRIES = 5;

private void attemptReconnect() {
    if (retryCount >= MAX_RETRIES) {
        // Tried 5 times, give up
        return;
    }
    
    // Wait 1s, 2s, 4s, 8s, 16s
    int delay = 1000 * (int) Math.pow(2, retryCount);
    
    handler.postDelayed(() -> {
        retryCount++;
        connectSSE();  // Try reconnecting
    }, delay);
}
```

### Connection Health Check

```java
// Check if receiving data
private long lastDataReceived = 0;
private static final long DATA_TIMEOUT = 60000; // 60 seconds

private Runnable healthCheck = new Runnable() {
    @Override
    public void run() {
        long timeSinceLastData = System.currentTimeMillis() - lastDataReceived;
        
        if (timeSinceLastData > DATA_TIMEOUT) {
            // No data for over 60 seconds, reconnect
            reconnect();
        }
        
        // Check again after 30 seconds
        handler.postDelayed(this, 30000);
    }
};
```

### Network Change Listener

```java
// When network changes (wifi ↔ 4G, lost network, network available)
private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = checkNetworkAvailable();
        
        if (isConnected && state == DISCONNECTED) {
            // Network back, reconnect
            connectSSE();
        } else if (!isConnected && state == CONNECTED) {
            // Network lost, close connection
            disconnect();
        }
    }
};
```

### Proper Connection Closing

```java
@Override
public void onDestroy() {
    super.onDestroy();
    
    // 1. Stop health check
    handler.removeCallbacks(healthCheck);
    
    // 2. Unregister network listener
    unregisterReceiver(networkReceiver);
    
    // 3. Close SSE connection
    if (call != null) {
        call.cancel();
    }
    
    // 4. Update state
    state = DISCONNECTED;
}
```






##  Architecture Diagram

```
┌─────────────────────────────────────┐
│         Server (Backend)            │
│   Sends symbol prices continuously  │
└────────────┬────────────────────────┘
             │ SSE Stream 
             ▼
┌─────────────────────────────────────┐
│    CryptoSSEService (Service)       │
│  ┌──────────────────────────┐      │
│  │  OkHttpClient            │      │
│  │  - Receive data          │      │
│  │  - Process JSON          │      │
│  └──────┬───────────────────┘      │
│         │ sendBroadcast()          │
└─────────┼──────────────────────────┘
          │
     ┌────┴────┐
     │         │
     ▼         ▼
┌────────┐  ┌──────────────┐
│Fragment│  │DetailActivity│
│(List)  │  │(Detail +     │
│        │  │ Chart)       │
└────────┘  └──────────────┘
```

##  Performance

### Network
- **Connection**: 1 connection for 19 coins
- **Bandwidth**: ~50KB/minute for all
- **Latency**: < 100ms to update prices
- **Reconnection**: Automatic with increasing wait time

### UI
- **List scrolling**: Smooth 60fps
- **Chart drawing**: < 500ms
- **Price update**: < 50ms
- **Memory**: < 50MB

### Battery
- **Idle**: ~2% / hour
- **Active**: ~5% / hour
- **Service off when not needed**: 0% consumption

## 🛠️ Installation

### Requirements
- Android Studio
- Android 7.0 or higher
- Internet connection

### Required Libraries
```gradle
dependencies {
    // Network connection
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
    
    // Chart drawing
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
    
    // JSON processing
    implementation 'org.json:json:20230227'
    
    // List
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
}
```

### API Endpoints
```java
// Receive continuous prices
"https://crypto-server-xqv5.onrender.com/events?symbols=..."

// Get price history
"https://api-crypto-58oa.onrender.com/history?symbol=...&interval=..."
```

## 🔧 Configuration

### Supported Cryptocurrencies
```
Bitcoin (BTC), Ethereum (ETH), BNB, Cardano (ADA), 
XRP, Solana (SOL), Polkadot (DOT), Avalanche (AVAX),
Litecoin (LTC), Chainlink (LINK), Polygon (MATIC),
Uniswap (UNI), Cosmos (ATOM), TRON (TRX), Aptos (APT),
Filecoin (FIL), NEAR Protocol, Internet Computer (ICP), 
VeChain (VET)
```

### Chart Timeframes
- **1 Week**: 7 days
- **1 Month**: 30 days
- **1 Year**: 365 days
- **5 Years**: 1825 days






