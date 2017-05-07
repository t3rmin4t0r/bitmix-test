# bitmix-test

```
Max collisons:10 vs 1
Total empties:25475700 vs 2108865
Collision count: 40323937 vs 0
```

Currently the simple shift implementation beats the more CPU intensive bit-mixing, because the extra avalanche properties of the more complex algorithm is entirely ignored by the power-of-two masking involved in the conversion from hash values to the actual bucket locations.
