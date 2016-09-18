# marvel #

Welcome to marvel! You have found the absolute command line tool to put some serious super hero experience into your shell! Remember, you become what you do. The more you use the `marvel` CLI, the sooner you'll be one!

![Code Like a Superhero](http://cdn.img.shop.marvel.com/content/ds/skyway/2014/category/full/fwb_Avengers_20140422.png)

## Getting Started

1. Request your API keys from the [Marvel Developer Portal](http://developer.marvel.com/account).
2. Store them in the following environment variables: `MARVEL_PUBLIC_KEY` and `MARVEL_PRIVATE_KEY`
   ```sh
   $ export MARVEL_PUBLIC_KEY=XXXXXXXX
   $ export MARVEL_PRIVATE_KEY=YYYYYYY
   ```
3. Clone this repository and at the root: `sbt stage`
4. `$ ./target/universal/stage/bin/marvel` will print the help
5. Enjoy! 

E.g. `$ ./target/universal/stage/bin/marvel all | grep <your name>`

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

## License ##

This code is open source software licensed under the [MIT License](https://opensource.org/licenses/MIT).

# Design and Technical Choices

## Caching

No caching here! We systematically go to Marvel's gateway. Mostly because I didn't see the point. I want to be able to pipe this command into others without having to wait for the last pagination round trip before processing the first elements. Had I convinced myself to pay the complexity cost of this feature, I'd have contemplated the use of some very basic "picklers" at first. Another option I use frequently with CLI to manage lists is sqlite. We can then use SQL to build more powerful commands.

## getopts

https://github.com/scopt/scopt is a fairly mature and standard option for one who wants to build a CLI in Scala but our project is really simplistic with only 2 modes hence we keep that option on the side at the moment.

## HTTP Client

This project is not serious enough on this layer. Pragmatically, it uses `io.Source.fromUrl` and even then it doesn't do much resilience / error management. To improve I'd look into http4s, young but seems consistent with Circe for JSON. If we were to have very strict requirements in terms of streaming or resilience, I'd go for Akka HTTP.

## JSON

I gave a try to Circe because I wanted to do so for a while. In the past I used spray-json but a benchmark I did few months ago proved it was really slow. I also used JSON4S but until it delivers on the promise of "binding them all" I don't really see the point. Circe just worked.

# Project Bootstrap Sequence
How I initiated this project.

```sh
mkdir marvel
cd marvel
sbt -sbt-create # using https://github.com/paulp/sbt-extras
> fresh name=marvel // using https://github.com/sbt/sbt-fresh
> ensimeConfig
```
