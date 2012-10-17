# _The Mole_ by GoldBlastGames

This project makes use of the echo library: https://github.com/oetzi/echo

## To build & run the game's components:

```bash
cd lib/echo
sbt publish-local
cd ../..
sbt run
```

## To package the server & client(s):

For a jar not containing any dependencies:
```bash
sbt package
```

For a jar containing all dependencies with unused classes stripped:
```bash
sbt proguard
```
