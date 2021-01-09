# hibernate-HHH-13134

Reproducer for [HHH-13134](https://hibernate.atlassian.net/browse/HHH-13134).

## Test Cases

Don't forget to enhance entities before running tests!

### HHH13134WithoutEnhancementAsProxyTestCase

Shows issues with `@LazyToOne` and join fetch if enhancement as proxy is disabled.

### HHH13134WithEnhancementAsProxyTestCase

Shows that enhancement as proxy solves the problems.
