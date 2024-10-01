db = connect('mongodb://localhost:27017/jokievents');

db.admins.insertMany([
    {
        name: "John Doe",
        email: "john.doe@example.com",
        role: "Super Admin"
    },
    {
        name: "Jane Smith",
        email: "jane.smith@example.com",
        role: "Admin"
    },
    {
        name: "Michael Johnson",
        email: "michael.johnson@example.com",
        role: "Admin"
    },
    {
        name: "Emily Davis",
        email: "emily.davis@example.com",
        role: "Super Admin"
    },
    {
        name: "Chris Wilson",
        email: "chris.wilson@example.com",
        role: "Admin"
    }
]);

// Insertar 5 clientes en la colección 'clients'
db.clients.insertMany([
    {
        idCard: "12345678A",
        name: "Alice Wonderland",
        address: "123 Fantasy Ave, Dreamland",
        phoneNumber: "555-1234",
        email: "alice@example.com",
        password: "securepassword1",
        idShoppingCart: "shopping_cart_1",
        active: true,
        verificationCode: "ABC123",
        verificationCodeExpiration: new Date("2024-12-31T23:59:59"),
        role: "CLIENT"
    },
    {
        idCard: "87654321B",
        name: "Bob Builder",
        address: "456 Construction St, Buildtown",
        phoneNumber: "555-5678",
        email: "bob@example.com",
        password: "securepassword2",
        idShoppingCart: "shopping_cart_2",
        active: false,
        verificationCode: "XYZ789",
        verificationCodeExpiration: new Date("2024-11-30T23:59:59"),
        role: "CLIENT"
    },
    {
        idCard: "12348765C",
        name: "Charlie Brown",
        address: "789 Comic Blvd, Snoopyville",
        phoneNumber: "555-6789",
        email: "charlie@example.com",
        password: "securepassword3",
        idShoppingCart: "shopping_cart_3",
        active: true,
        verificationCode: "LMN456",
        verificationCodeExpiration: new Date("2024-10-15T23:59:59"),
        role: "CLIENT"
    },
    {
        idCard: "87651234D",
        name: "Dorothy Gale",
        address: "123 Oz St, Emerald City",
        phoneNumber: "555-9876",
        email: "dorothy@example.com",
        password: "securepassword4",
        idShoppingCart: "shopping_cart_4",
        active: true,
        verificationCode: "QRS852",
        verificationCodeExpiration: new Date("2024-09-30T23:59:59"),
        role: "CLIENT"
    },
    {
        idCard: "65432198E",
        name: "Eve Adams",
        address: "321 Paradise Rd, Eden",
        phoneNumber: "555-4321",
        email: "eve@example.com",
        password: "securepassword5",
        idShoppingCart: "shopping_cart_5",
        active: false,
        verificationCode: "TUV963",
        verificationCodeExpiration: new Date("2024-08-31T23:59:59"),
        role: "CLIENT"
    }
]);
