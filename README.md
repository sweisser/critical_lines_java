Critical Lines Algorithm
---
Port of the VBA code in the book "Mean-Variance Analysis in Portfolio Choice and Capital Markets" by Harry M. Markowitz.

The code is not very well tested, so use it for educational purposes only.

The test case provided in the book works.

The code works, but could still use more refactoring...

Main goal was to port the VBA code to a working solution. The main challenge was to get the array indexes right.
In VBA, array indices start at 1, whereas in Java they start at 0. I have adjusted all the index logic. But since I only
have the test case in the book, there may still be some bugs. So be careful when using the code.
