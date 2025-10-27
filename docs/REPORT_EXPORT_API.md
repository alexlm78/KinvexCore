# Report Export API Documentation

## Overview

The Report Export API allows users to export various types of reports in PDF and Excel formats. This functionality implements requirement 4.5: Export reports in PDF and Excel formats.

## Endpoint

**POST** `/api/reports/export`

### Authentication

Requires authentication with `MANAGER` or `ADMIN` role.

### Request Body

```json
{
  "reportType": "INVENTORY_MOVEMENTS",
  "format": "PDF",
  "startDate": "2024-01-01T00:00:00",
  "endDate": "2024-01-31T23:59:59",
  "title": "Monthly Inventory Report",
  "detailed": true,
  "productIds": [1, 2, 3],
  "supplierIds": [1, 2],
  "limit": 1000
}
```

### Request Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `reportType` | String | Yes | Type of report: `INVENTORY_MOVEMENTS`, `STOCK_LEVELS`, `SUPPLIER_PERFORMANCE` |
| `format` | String | Yes | Export format: `PDF`, `EXCEL` |
| `startDate` | DateTime | No | Start date for the report period (ISO format) |
| `endDate` | DateTime | No | End date for the report period (ISO format) |
| `title` | String | No | Custom title for the report |
| `detailed` | Boolean | No | Include detailed information (applies to inventory movements) |
| `productIds` | Array | No | Filter by specific product IDs |
| `productCodes` | Array | No | Filter by specific product codes |
| `supplierIds` | Array | No | Filter by specific supplier IDs |
| `categoryIds` | Array | No | Filter by specific category IDs |
| `movementTypes` | Array | No | Filter by movement types: `IN`, `OUT` |
| `referenceTypes` | Array | No | Filter by reference types |
| `sourceSystems` | Array | No | Filter by source systems |
| `activeProductsOnly` | Boolean | No | Include only active products |
| `activeSuppliersOnly` | Boolean | No | Include only active suppliers |
| `limit` | Integer | No | Maximum number of records to include |

### Response

Returns the exported file as binary data with appropriate headers:

- **Content-Type**: `application/pdf` for PDF files, `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` for Excel files
- **Content-Disposition**: `attachment; filename="report_filename.pdf"` or `attachment; filename="report_filename.xlsx"`

### Example Usage

#### Export Inventory Movements to PDF

```bash
curl -X POST "http://localhost:8080/api/reports/export" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportType": "INVENTORY_MOVEMENTS",
    "format": "PDF",
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-01-31T23:59:59",
    "title": "January Inventory Movements",
    "detailed": true
  }' \
  --output inventory_movements.pdf
```

#### Export Stock Levels to Excel

```bash
curl -X POST "http://localhost:8080/api/reports/export" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportType": "STOCK_LEVELS",
    "format": "EXCEL",
    "activeProductsOnly": true
  }' \
  --output stock_levels.xlsx
```

#### Export Supplier Performance to PDF

```bash
curl -X POST "http://localhost:8080/api/reports/export" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "reportType": "SUPPLIER_PERFORMANCE",
    "format": "PDF",
    "startDate": "2024-01-01T00:00:00",
    "endDate": "2024-03-31T23:59:59",
    "title": "Q1 Supplier Performance Report",
    "supplierIds": [1, 2, 3]
  }' \
  --output supplier_performance.pdf
```

## Report Types

### 1. Inventory Movements (`INVENTORY_MOVEMENTS`)

Exports inventory movement data including:
- Movement ID
- Product code and name
- Movement type (IN/OUT)
- Quantity
- Date and time
- Unit price

When `detailed` is true, also includes:
- Reference type and ID
- Source system
- User who created the movement
- Notes

### 2. Stock Levels (`STOCK_LEVELS`)

Exports current stock level data including:
- Product code and name
- Category
- Current stock
- Minimum and maximum stock levels
- Unit price
- Movement statistics for the period (if date range provided)
- Last movement date

### 3. Supplier Performance (`SUPPLIER_PERFORMANCE`)

Exports supplier performance metrics including:
- Supplier name and contact information
- Total orders
- Completed, pending, and cancelled orders
- Total and average order values
- Performance metrics

## Error Responses

### 400 Bad Request
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Report type is required"
}
```

### 403 Forbidden
```json
{
  "error": "ACCESS_DENIED",
  "message": "Access denied"
}
```

### 500 Internal Server Error
```json
{
  "error": "EXPORT_ERROR",
  "message": "Error generating export file"
}
```

## File Naming Convention

Generated files follow this naming pattern:
- `{ReportType}_{YYYYMMDD_HHMM}.{extension}`

Examples:
- `Movimientos_Inventario_20240115_1430.pdf`
- `Niveles_Stock_20240115_1430.xlsx`
- `Desempeño_Proveedores_20240115_1430.pdf`

## Technical Implementation

The export functionality uses:
- **Apache POI** for Excel file generation
- **iText 7** for PDF file generation
- **Spring Boot** for REST API endpoints
- **Spring Security** for authentication and authorization

Files are generated in-memory and streamed directly to the client without being stored on the server.
