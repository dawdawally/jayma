# Testing Guide

This document outlines the testing strategy and how to run tests for the Jayma POS application.

## Test Structure

### Unit Tests (`app/src/test/`)
- **Repository Tests**: Test data layer logic with mocked dependencies
  - `ProductRepositoryTest.kt` - Product repository operations
  - `SaleRepositoryTest.kt` - Sale repository operations
  
- **ViewModel Tests**: Test UI state management
  - `ProductViewModelTest.kt` - Product list and search functionality

### Integration Tests (`app/src/androidTest/`)
- **Database Tests**: Test Room database operations
  - `AppDatabaseTest.kt` - Database CRUD operations, queries, relationships

- **UI Tests**: Test user interface interactions
  - `ProductListFragmentTest.kt` - Product list UI interactions

## Running Tests

### Run All Unit Tests
```bash
./gradlew test
```

### Run All Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific Test Class
```bash
./gradlew test --tests "com.jayma.pos.data.repository.ProductRepositoryTest"
```

### Run Tests with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## Test Coverage Goals

- **Unit Tests**: > 70% coverage for repositories and ViewModels
- **Integration Tests**: Cover all critical database operations
- **UI Tests**: Cover main user flows (product browsing, cart, checkout)

## Test Scenarios Covered

### Unit Tests
✅ Product repository CRUD operations  
✅ Product sync with pagination  
✅ Sale repository operations  
✅ Sale sync/upload logic  
✅ ViewModel state management  
✅ Search and filter functionality  

### Integration Tests
✅ Database insert/retrieve operations  
✅ Unsynced sales query  
✅ Product search by code/barcode  
✅ Data relationships and integrity  

### UI Tests
✅ Product list display  
✅ Search functionality  
✅ Barcode scanner button  

## Future Test Additions

### Remaining Test Scenarios
- [ ] Offline mode testing
- [ ] Network failure handling
- [ ] Cart operations
- [ ] Checkout flow
- [ ] Receipt printing
- [ ] Background sync workers
- [ ] Multi-tenant data isolation
- [ ] Performance benchmarks

## Mock Data

Test data is created using helper functions in test files:
- `createTestProduct()` - Creates mock product entities
- `createTestSale()` - Creates mock sale entities
- `createMockApiResponse()` - Creates mock API responses

## Testing Best Practices

1. **Isolation**: Each test should be independent
2. **Naming**: Use descriptive test names (`functionName_scenario_expectedResult`)
3. **Arrange-Act-Assert**: Follow AAA pattern
4. **Mocking**: Mock external dependencies (API, database)
5. **Coverage**: Aim for high coverage on critical paths

## Continuous Integration

Tests should be run:
- Before every commit (pre-commit hook recommended)
- On every pull request
- Before deployment

## Troubleshooting

### Tests Failing Locally
- Ensure Android SDK is properly configured
- Check that test dependencies are synced
- Verify emulator/device is connected for instrumented tests

### Mock Issues
- Use `mockito-inline` for final class mocking
- Ensure coroutines are properly handled with `runTest`
