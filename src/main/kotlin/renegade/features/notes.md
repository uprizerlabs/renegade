### Notes

Starting with most predictive features, try a variety of math combinators, working down
the feature pairwise (have that code somewhere?) for the combinators.  Once one is found that is
unique and better than its constituent features (measured individually), add it to the features
and start again (but don't try the same things twice).

INTRINSIC LEARNING - INTJ for short

-----------------

* bifunc
  * add
    * F1,F2
      * F1+F2
        * X(F1)+F2
          * X=-X
          * X=sin(X)
          * X=tan(X)
      * F1*F2
      * F1/F2
      * F1-F2
    * F1,F3
    * F2,F3
  * mult
  * div
  * pow