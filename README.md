# OFI Rota Manager (prototype)

Applikasjonen er utviklet etter forespørsel fra en potensiell arbeidsgiver for å demonstrere tekniske ferdigheter, samtidig som den adresserer et reelt behov - utvikleren har benyttet anledning til å levere en prototype av en mer 
omfattende løsning som er under arbeid.

OFI Rota Manager har som mål å assistere butikkdrivere med å organisere sine ansattes skiftplaner på en tidseffektiv og brukervennlig måte. Den kommer med en praktisk og intuitiv brukergrensesnitt som muliggjør raskt generering av excel baserte tjenesteplaner.

## Bruk
Applikasjonen er i produksjon på:

https://ofirotamanager.herokuapp.com/

Grensesnittet skal være intuitivt. For å populere dagene med skifter benyttes knappen "Legg til ny skift" og deretter tastes inn navn på ansatt, start tid og slutt tid. Skiftet kan gjøres repeterbar ved å kopiere til relevante dager. Etter å ha lagt inn et skift, er det også mulig å endre eller slette skiftet samt flytte den til andre dager med drag&drop.

Etter at alle skiftene er lagt inn kan knappen "Generer tjenesteplan" benyttes for å produsere en excel fil som inneholder en skiftplan i en tabell med predefinert format, klar for utskrift. Ytterligere justeringer kan utføres direkte i excel.

## Teknologier og rammeverk
Applikasjonen bygger seg på 
- [JAVA 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Spring Boot/REST](https://projects.spring.io/spring-boot/)
- [Maven](https://maven.apache.org/)
- [Apache POI](https://poi.apache.org/)
- [AngularJs](https://angularjs.org/)
- [Angular Bootstrap Calendar](https://github.com/mattlewis92/angular-bootstrap-calendar)
- [Bootstrap](http://getbootstrap.com/)
- [Bower](https://bower.io/)

## Lokal utvikling
Applikasjonen kan enkelt kjøres lokalt med Spring boot, ved å klone den og bygge den med Maven:

`mvn clean install`

Det er også mulig å invokere main metoden i App.java direkte.

Bower komponenter er sjekket inn, så det skal ikke være behov for å installere disse via bower. 

## Continuous deployment
Alle commits til dev-branch blir auto deployet til produksjon via Heroku.





