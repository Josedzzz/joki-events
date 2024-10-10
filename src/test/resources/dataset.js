db = connect('mongodb://localhost:27017/jokievents-testing');

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
            "_id": {
                "$oid": "6706a5101654657267419fef"
            },
            "idCard": "AB123456",
            "name": "new-name-n3on431408",
            "address": "Casa de 431408",
            "phoneNumber": "3141431408",
            "email": "greatEmail431408@example.com",
            "password": "$2a$10$0xqYdGWwT7vnz3COTlUeje/1gykjDOasab8rjzm16jeEbsX60TRZu",
            "idShoppingCart": "6706afa4ba774f520afb4ca2",
            "active": false,
            "verificationCode": "390209",
            "verificationCodeExpiration": {
                "$date": "2024-10-10T03:04:54.417Z"
            },
            "role": "CLIENT",
            "_class": "com.uq.jokievents.model.Client"
    },
        {
            "_id": {
                "$oid": "66f3b71c95dcb9591580d078"
            },
            "idCard": "1097032971",
            "name": "joki",
            "address": "quimbaya",
            "phoneNumber": "1231231234",
            "email": "balinius11@gmail.com",
            "password": "$2a$10$GZODpgapTfxgrDS167XnrOJRFXGBQ4m8q6ZFdOhc36wWxAIgpirTq",
            "idShoppingCart": "66f3b71795dcb9591580d077",
            "active": true,
            "role": "CLIENT",
            "_class": "com.uq.jokievents.model.Client"
    }
],

db.coupons.insertMany [
    {
        "_id": {
            "$oid": "67054eb8734d84764f8b0316"
        },
        "name": "382124133444",
        "discountPercent": 25,
        "expirationDate": {
            "$date": "2025-01-01T04:59:59.000Z"
        },
        "minPurchaseAmount": 150,
        "isUsed": false,
        "_class": "com.uq.jokievents.model.Coupon"
    }
],

db.events.insertMany [
    {
        "_id": {
            "$oid": "6705590b39e3c64472be8665"
        },
        "name": "Updated Event Name 726643838294",
        "address": "Updated City",
        "city": "123 Updated Address",
        "eventDate": {
            "$date": "2024-10-09T16:42:54.098Z"
        },
        "availableForPurchase": true,
        "localities": [
            {
                "name": "ULTRA VIP726643838294",
                "price": 100,
                "maxCapacity": 50,
                "currentOccupancy": 0
            }
        ],
        "totalAvailablePlaces": 100,
        "eventImageUrl": "https://firebasestorage.googleapis.com/v0/b/joki-events-img-repo.appspot.com/o/a2d8f062-66c1-4cca-8da4-016be0d32e60-s.png?alt=media",
        "localitiesImageUrl": "https://firebasestorage.googleapis.com/v0/b/joki-events-img-repo.appspot.com/o/a17b040e-be26-4b98-af33-da0a7885bb31-s.png?alt=media",
        "eventType": "CONCERT",
        "_class": "com.uq.jokievents.model.Event"
    },

    {
        "_id": {
            "$oid": "67072eac29bb6b12ddff0f9d"
        },
        "name": "Great Feid Music Concert",
        "address": "123 Main Street",
        "city": "New York",
        "eventDate": {
            "$date": "2024-12-16T01:00:00.000Z"
        },
        "availableForPurchase": true,
        "localities": [
            {
                "name": "VIP",
                "price": 200,
                "maxCapacity": 100,
                "currentOccupancy": 0
            },
            {
                "name": "General Admission",
                "price": 50,
                "maxCapacity": 4890,
                "currentOccupancy": 0
            }
        ],
        "totalAvailablePlaces": 5000,
        "eventImageUrl": "https://firebasestorage.googleapis.com/v0/b/joki-events-img-repo.appspot.com/o/8e9e6269-60e0-41b6-b190-5f55cf18bd87-s.png?alt=media",
        "localitiesImageUrl": "https://firebasestorage.googleapis.com/v0/b/joki-events-img-repo.appspot.com/o/d67942ee-4377-4d39-9102-3000ec09a0e4-s.png?alt=media",
        "eventType": "CONCERT",
        "_class": "com.uq.jokievents.model.Event"
    }
]
);
