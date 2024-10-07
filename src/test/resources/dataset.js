db = connect('mongodb://localhost:27017/jokievents');

// This has to be updated

db.admins.insertMany([
    {
        "_id": {
            "$oid": "66f3aeb160c236c93c22b808"
        },
        "email": "balinius@gmail.com",
        "username": "balineroo",
        "password": "$2a$10$Y13QZWu/m5OiK5zCy3lux.DdEUDN7ciLb3MUXWlgpzPKhz4nDmkdq", //1234
        "active": true,
        "role": "ADMIN",
        "_class": "com.uq.jokievents.model.Admin"
    },
    {
        "_id": {
            "$oid": "66f3aeb160c236c93c22b807"
        },
        "email": "balinius11@gmail.com",
        "username": "balin",
        "password": "$2a$10$Y13QZWu/m5OiK5zCy3lux.DdEUDN7ciLb3MUXWlgpzPKhz4nDmkdq", //1234
        "active": true,
        "role": "ADMIN",
        "_class": "com.uq.jokievents.model.Admin"
    }
]);

// Insertar 5 clientes en la colección 'clients'
db.clients.insertMany([
    {
        "_id": {
            "$oid": "66f3b71c95dcb9591580d078"
        },
        "idCard": "1097032971",
        "name": "joki",
        "phoneNumber": "1231231234",
        "email": "balinius11@gmail.com",
        "password": "$2a$10$GZODpgapTfxgrDS167XnrOJRFXGBQ4m8q6ZFdOhc36wWxAIgpirTq",
        "idCoupons": [],
        "idShoppingCart": {
            "$oid": "66f3b71795dcb9591580d077"
        },
        "active": true,
        "role": "CLIENT",
        "_class": "com.uq.jokievents.model.Client",
        "address": "quimbaya"
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
