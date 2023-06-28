// AUTO-GENERATED FILE. DO NOT MODIFY.

// This file is an auto-generated file by Ballerina persistence layer for model.
// It should not be modified by hand.

public type Manufacture record {|
    readonly string id;
    string productsId;
|};

public type ManufactureOptionalized record {|
    string id?;
    string productsId?;
|};

public type ManufactureWithRelations record {|
    *ManufactureOptionalized;
    ProductOptionalized products?;
|};

public type ManufactureTargetType typedesc<ManufactureWithRelations>;

public type ManufactureInsert Manufacture;

public type ManufactureUpdate record {|
    string productsId?;
|};

public type Product record {|
    readonly string id;
|};

public type ProductOptionalized record {|
    string id?;
|};

public type ProductWithRelations record {|
    *ProductOptionalized;
    ManufactureOptionalized[] manufacture?;
|};

public type ProductTargetType typedesc<ProductWithRelations>;

public type ProductInsert Product;

public type ProductUpdate record {|
|};

