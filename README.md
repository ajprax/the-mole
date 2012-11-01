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

## Game settings file:

<!--
     This is an example game configuration file for GoldBlastGame's 'The Mole'.
  -->
<game>
  <port>2552</port>

  <players>
    <!-- Soviet team -->
    <player name="kane" camp="America" allegiance="USSR">
      <skills>
        <skill name="wetwork"               min="4" max="10" />
        <skill name="information-gathering" min="4" max="10" />
        <skill name="sexitude"              min="4" max="10" />
        <skill name="stoicism"              min="4" max="10" />
        <skill name="sabotage"              min="4" max="10" />
        <skill name="subterfuge"            min="4" max="10" />
      </skills>
      <abilities>
        <ability name="secretive" />
        <ability name="radio-jamming" />
      </abilities>
    </player>
    <player name="robert" camp="USSR" allegiance="USSR">
      <skills>
        <skill name="wetwork"               min="4" max="10" />
        <skill name="information-gathering" min="4" max="10" />
        <skill name="sexitude"              min="4" max="10" />
        <skill name="stoicism"              min="4" max="10" />
        <skill name="sabotage"              min="4" max="10" />
        <skill name="subterfuge"            min="4" max="10" />
      </skills>
      <abilities>
        <ability name="secretive" />
        <ability name="radio-jamming" />
      </abilities>
    </player>
    <player name="franklin" camp="USSR" allegiance="USSR">
      <skills>
        <skill name="wetwork"               min="4" max="10" />
        <skill name="information-gathering" min="4" max="10" />
        <skill name="sexitude"              min="4" max="10" />
        <skill name="stoicism"              min="4" max="10" />
        <skill name="sabotage"              min="4" max="10" />
        <skill name="subterfuge"            min="4" max="10" />
      </skills>
      <abilities>
        <ability name="secretive" />
        <ability name="radio-jamming" />
      </abilities>
    </player>

    <!-- American team -->
    <player name="aaron" camp="USSR" allegiance="America">
      <skills>
        <skill name="wetwork"               min="4" max="10" />
        <skill name="information-gathering" min="4" max="10" />
        <skill name="sexitude"              min="4" max="10" />
        <skill name="stoicism"              min="4" max="10" />
        <skill name="sabotage"              min="4" max="10" />
        <skill name="subterfuge"            min="4" max="10" />
      </skills>
      <abilities>
        <ability name="secretive" />
        <ability name="radio-jamming" />
      </abilities>
    </player>
    <player name="william" camp="America" allegiance="America">
      <skills>
        <skill name="wetwork"               min="4" max="10" />
        <skill name="information-gathering" min="4" max="10" />
        <skill name="sexitude"              min="4" max="10" />
        <skill name="stoicism"              min="4" max="10" />
        <skill name="sabotage"              min="4" max="10" />
        <skill name="subterfuge"            min="4" max="10" />
      </skills>
      <abilities>
        <ability name="secretive" />
        <ability name="radio-jamming" />
      </abilities>
    </player>
    <player name="daisy" camp="America" allegiance="America">
      <skills>
        <skill name="wetwork"               min="4" max="10" />
        <skill name="information-gathering" min="4" max="10" />
        <skill name="sexitude"              min="4" max="10" />
        <skill name="stoicism"              min="4" max="10" />
        <skill name="sabotage"              min="4" max="10" />
        <skill name="subterfuge"            min="4" max="10" />
      </skills>
      <abilities>
        <ability name="secretive" />
        <ability name="radio-jamming" />
      </abilities>
    </player>
  </players>

  <session>
    <dropFreq>14400000</dropFreq>
    <missionFreq>86400000</missionFreq>
  </session>
</game>
