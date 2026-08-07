```mermaid
erDiagram
    USER ||--o{ TRIP : owns
    
    USER {
        uuid id PK
        string name
        string email UK
        string password "Hashed"
    }

    TRIP {
        uuid id PK
        string title
        string destination
        date startDate
        date endDate
        decimal budget
        string description "Optional"
        uuid userId FK "Owner"
    }
```