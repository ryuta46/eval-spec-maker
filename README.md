# eval-spec-maker

Converts evaluation specifications written with markdown to xlsx table.

## Usage 

### Write Markdown

Write markdown as follows 
```test_spec.md
# Category

## Major 
### Middle
#### Minor

1. Test process 1
2. Test process 2
* [ ] Check point 1
* [ ] Check point 2
```
Now h1, h2, h3, h4, itemization(1., 2. ... n.) and check marks(*[ ]) are supported.

* A h1 element means evaluation category. This will be converted to a sheet in xlsx.
* h2, h3 and h4 elements mean major item, middle item and minor item respectively.
* Items beginning with "n." mean test procedures.
* Items beginning with "* [ ]" mean check points of this test.

### Convert markdown to xlsx

Usage
```
java -jar evalSpecMaker.jar test_spec.md test_spec.xlsx
```


## Limitations

Now, headers in converted xlsx table are Japanese only.
