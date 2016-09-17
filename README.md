# marvel #

Welcome to marvel! You have found the absolute command line tool to put some serious super hero experience into your shell! Remember, you become what you do. The more you use the `marvel` CLI, the sooner you'll be one!

## Getting Started

1. Request your API keys from the [Marvel Developer Portal](http://developer.marvel.com/account).
2. Store them in the following environment variables: `MARVEL_PUBLIC_KEY` and `MARVEL_PRIVATE_KEY`
   ```sh
   $ export MARVEL_PUBLIC_KEY=XXXXXXXX
   $ export MARVEL_PRIVATE_KEY=YYYYYYY
   ```
3. `sbt run`

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [MIT License](https://opensource.org/licenses/MIT).

# Project Setup

```sh
mkdir marvel
cd marvel
sbt -sbt-create # using https://github.com/paulp/sbt-extras
> fresh name=marvel // using https://github.com/sbt/sbt-fresh
> ensimeConfig
```

# Design and Technical Choices

## getopts

https://github.com/scopt/scopt is a fairly mature and standard option for one who wants to build a CLI but our project is really simplistic with only 2 modes hence we keep that option on the side at the moment.

## HTTP Client

https://github.com/megamsys/newman/ seems to be the new cool kid for async HTTP, superseding the cryptic dispatch. I have never used it though hence not sure it will help more than it will hinder. Spray client, I have used it many time but feel overkill for now. https://github.com/scalaj/scalaj-http, not used, sync.

## JSON

I gave a try to Circe because I wanted to do so for a while. In the past I used spray-json but a benchmark I did few months ago proved it was really slow. I also used JSON4S but until it delivers on the promise of "binding them all" I don't really see the point. 