import ballerina/persist as _;

public type Manufacture record {|
    readonly string id;
    Product products;
|};

public type Product record {|
    readonly string id;
	Manufacture[] manufacture;
|};

public type Product1 record {|
    readonly string id;
	Manufacture[] manufacture;
|};
