# WuZhu 🪙

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.10-brightgreen)](https://spring.io/projects/spring-boot)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-Apache%202.0-red)](LICENSE)

**WuZhu** is a cryptocurrency trading journal and portfolio analysis desktop application designed for crypto traders and investors. It provides comprehensive tools to record, track, and analyze your cryptocurrency transactions with real-time market data.

<p align="center">
  <img src="src/main/resources/org/lifxue/wuzhu/images/logo.png" alt="WuZhu Logo" width="120">
</p>

---

## 📋 Table of Contents

- [Features](#-features)
- [Screenshots](#-screenshots)
- [Installation](#-installation)
  - [Ubuntu](#ubuntu-2404)
  - [Windows](#windows)
- [Development](#-development)
- [Usage Guide](#-usage-guide)
- [Architecture](#-architecture)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### 📊 Trading Management
- **Trading Records**: Record buy/sell transactions with detailed information (date, price, quantity, trading pair)
- **Cash Management**: Track deposits and withdrawals to/from exchanges
- **Import/Export**: Support CSV format for data migration and backup

### 📈 Analysis & Visualization
- **Portfolio Pie Chart**: Visual representation of your portfolio allocation with current market value
- **Profit/Loss Analysis**: Calculate average cost, current P&L, and percentage change
- **Multi-currency Support**: Filter and analyze transactions by specific trading pairs

### 💼 Market Data Integration
- **CoinMarketCap API**: Real-time cryptocurrency price data from CoinMarketCap
- **Proxy Support**: HTTP proxy configuration for accessing CoinMarketCap API
- **Customizable Update**: Automatic or manual price updates based on preferences

### 🛠️ Additional Features
- **Rich Text Notes**: Built-in notepad for recording trading strategies and memos
- **Theme Switching**: Light and dark theme support
- **Multi-language Ready**: Internationalization support
- **Small Coin Filtering**: Option to hide small value coins in portfolio view

---

## 📸 Screenshots

*Screenshots will be added here*

---

## 🚀 Installation

### Prerequisites

- **Java 21 JDK** (Required for running the application)
- **Maven 3.8+** (Optional, project includes Maven Wrapper)

### Ubuntu 24.04

#### Option 1: Using .deb Package (Recommended)

```bash
# Download the latest release
curl -LO https://github.com/lifxue/WuZhu/releases/download/v1.0.0/wuzhu_1.0.0_amd64.deb

# Install the package
sudo dpkg -i wuzhu_1.0.0_amd64.deb

# Fix dependencies if needed
sudo apt-get install -f

# Launch the application
wuzhu
```

#### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/lifxue/WuZhu.git
cd WuZhu

# Build the project
./mvnw clean package -DskipTests

# Run the application
java -jar target/WuZhu-1.0.jar
```

### Windows

#### Option 1: Using .msi Installer (Recommended)

1. Download the latest `.msi` installer from [Releases](https://github.com/lifxue/WuZhu/releases)
2. Run the installer and follow the setup wizard
3. Launch WuZhu from the Start Menu or Desktop shortcut

#### Option 2: Build from Source

```powershell
# Clone the repository
git clone https://github.com/lifxue/WuZhu.git
cd WuZhu

# Build the project
.\mvnw.cmd clean package -DskipTests

# Run the application
java -jar target\WuZhu-1.0.jar
```

---

## 🛠️ Development

### Environment Setup

#### Required Software

| Software | Minimum Version | Notes |
|----------|----------------|-------|
| Java JDK | 21 | Must use JDK (not JRE), recommended: BellSoft Liberica JDK 21 Full |
| Maven | 3.8+ | Optional, project includes Maven Wrapper (`./mvnw`) |
| Git | Any | For cloning the repository |

### Quick Start (Cross-Platform)

```bash
# 1. Clone the repository
git clone https://github.com/lifxue/WuZhu.git
cd WuZhu

# 2. Build the project
./mvnw clean package -DskipTests

# 3. Run the application
./mvnw spring-boot:run
# OR
java -jar target/WuZhu-1.0.jar
```

### Development Commands

```bash
# Clean and compile (no packaging)
./mvnw clean compile

# Run tests
./mvnw test

# Package (skip tests for quick build)
./mvnw clean package -DskipTests

# Full build workflow
./mvnw clean compile test package

# Debug mode (port 5005)
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

### IDE Configuration

#### IntelliJ IDEA

1. Import project: `File -> Open` select `pom.xml`
2. Enable annotation processing: `Settings -> Build -> Annotation Processors -> Enable`
3. Set JDK: `Project Structure -> SDKs` add JDK 21

#### VS Code

Recommended extensions:
- Extension Pack for Java
- Spring Boot Extension Pack
- Lombok Annotations Support

---

## 📖 Usage Guide

### First Time Setup

1. **Launch the application** - WuZhu will automatically create the database on first run
2. **Configure API Key** - Go to Settings (偏好设置) and enter your CoinMarketCap API Key
   - Get your free API key from: https://pro.coinmarketcap.com/signup
3. **Select Coins** - Go to Select Coin (币种选择) to choose which cryptocurrencies to track
4. **Start Recording Trades** - Use the Trading (交易信息) module to record your transactions

### Module Guide

#### 📝 Trading Info (交易信息)
Record and manage your cryptocurrency buy/sell transactions.
- Select base and quote currencies
- Enter price, quantity, and date
- View all transactions in a table format
- Edit or delete existing records

#### 💰 Cash (现金)
Track deposits and withdrawals to/from exchanges.
- Record deposits (入金) and withdrawals (出金)
- Manage USDT transactions separately
- Historical record tracking

#### 📊 Statistics (统计分析)
Analyze your portfolio performance.
- View profit/loss for specific coins or entire portfolio
- Filter by date range and transaction type
- Display average cost, current value, and percentage change

#### 🥧 Pie Chart (饼图)
Visual representation of portfolio allocation.
- Shows current market value of each holding
- Displays percentage allocation
- Hover over segments for detailed information
- Option to hide small value coins

#### 🔍 Select Coin (币种选择)
Choose which cryptocurrencies to track and display.
- Search for specific coins
- Enable/disable coins with checkboxes
- Data synced from CoinMarketCap

#### ⚙️ Preferences (偏好设置)
Configure application settings.
- Theme selection (Light/Dark)
- CoinMarketCap API Key
- Proxy settings
- Auto-update price data
- Small coin filtering threshold
- Database initialization

#### 📝 Note (笔记)
Built-in rich text editor for notes.
- Strategy tab for trading strategies
- Memo tab for general notes
- Rich text formatting support

#### 📁 Import/Export
Import and export trading data.
- Export all transaction records to CSV
- Import transaction records from CSV
- Data backup and migration support

---

## 🏗️ Architecture

### Technology Stack

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Programming Language |
| Spring Boot | 2.7.10 | Application Framework |
| JavaFX | 21.0.2 | Desktop UI Framework |
| WorkbenchFX | 11.3.1 | Workbench-style UI Framework |
| H2 Database | 2.2.220 | Embedded Database |
| OpenFeign | 2021.0.3 | HTTP Client for API |
| Lombok | 1.18.30 | Code Generation |
| MapStruct | 1.5.5 | Object Mapping |
| RichTextFX | 0.11.0 | Rich Text Editing |

### Project Structure

```
WuZhu/
├── src/main/java/org/lifxue/wuzhu/
│   ├── config/           # Configuration (Feign, Proxy)
│   ├── convert/          # MapStruct converters
│   ├── dto/              # Data Transfer Objects
│   ├── enums/            # Enumerations
│   ├── modules/          # Feature modules (8 business modules)
│   │   ├── cash/         # Cash management
│   │   ├── file/         # Import/Export
│   │   ├── note/         # Notes (Rich Text)
│   │   ├── piechart/     # Portfolio pie chart
│   │   ├── selectcoin/   # Coin selection
│   │   ├── setting/      # Preferences
│   │   ├── statistics/   # Statistical analysis
│   │   └── tradeinfo/    # Trading records
│   ├── pojo/             # JPA Entity Classes
│   ├── repository/       # Data Access Layer
│   ├── service/          # Service Layer
│   ├── springfx/         # Spring-JavaFX Integration
│   └── util/             # Utility Classes
└── src/main/resources/   # FXML, CSS, Configurations
```

### Application Architecture

```
WuZhuApplication.main()
    ↓
Application.launch(JavaFxApplication.class)
    ↓
JavaFxApplication.init() → Initialize Spring Context
    ↓
JavaFxApplication.start() → Publish StageReadyEvent
    ↓
PrimaryStageInitializer → Initialize WorkbenchFX + Load Modules
```

### Database

- **Type**: H2 Embedded Database
- **File Location**: `~/.wuzhu/h2/wuzhudbjpa`
- **Schema Mode**: `ddl-auto: update` (automatic schema updates)
- **Backup**: Simply copy the `.wuzhu` directory

### API Integration

The application integrates with **CoinMarketCap API** for real-time cryptocurrency data:

- **API Endpoint**: https://pro-api.coinmarketcap.com
- **Authentication**: API Key via `X-CMC_PRO_API_KEY` header
- **Rate Limits**: Subject to CoinMarketCap API tier

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [CoinMarketCap](https://coinmarketcap.com/) for providing cryptocurrency market data
- [Spring Boot](https://spring.io/projects/spring-boot) for the excellent framework
- [JavaFX](https://openjfx.io/) for the modern desktop UI framework
- [WorkbenchFX](https://github.com/dlsc-software-consulting-gmbh/WorkbenchFX) for the workbench-style UI

---

## 📞 Support

If you encounter any issues or have questions, please [open an issue](https://github.com/lifxue/WuZhu/issues) on GitHub.

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/lifxue">lifxue</a>
</p>
