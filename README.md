# scannerapp
Front-end Logistics application to manage inflow of stock, using google machine learning based vision and data modeling.
This application contains
1.  A barcode scanner which automatically uses the google api crop_hints and ml_kit to analyze and parse various forms of barcores
2.  Failing that, defualts to a rudimentary scan of the book cover and matches to the appropriate book from a database using 4 factors.
    i)  Euclidian Distance between text features and the standard logo;
    ii) Latin Text detected;
    iii)Labels identified by mk_kit;
    iv) The average color of the cover, seperated into 16 blocks;
    iv) Characters identified by custom machine learning model
3.  Failing that, goes to the manual entry of a 4 digit code corresponding to the stock item with the appropriate verification and validation that is required.

Data Model was trained with 4 labels in tensorflow.Lite

