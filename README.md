# scannerapp
Logistics application to manage inflow of stock using google machine learning based vision and LIRE.
This application contains
1.  A barcode scanner which automatically uses the google api crop_hints and ml_kit to analyze and parse various forms of barcores
2.  Failing that, defualts to a rudimentary scan of the book cover and matches to the appropriate book from a database using CBIR from the LIRE library.
3.  Failing that, goes to the manual entry of a 4 digit code corresponding to the stock item with the appropriate verification and validation required.
  
