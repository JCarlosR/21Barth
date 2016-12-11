Versión inicial (offline) (1 jugador y dealer)
---
La pantalla se encontraba repartida como lo muestra la imagen siguiente:

En este caso, en la parte superior izquierda se encuentra el dealer y su score representado por un signo de interrogación.

Debajo de las cartas del dealer se encuentra la apuesta realizada por el jugador. 
El jugador ganará o perderá esa cantidad.

Centrado verticalmente hacia la derecha se encuentra el balance del jugador. 
Si llega a 0, pierde.

En el lado inferior izquierdo se encuentran las cartas del jugador.
Y un poco más a la derecha, su score actual.

Finalmente, en la parte inferior se encuentran las opciones del juego.


Versión realtime (online) (2 jugadores y dealer)
---
Los elementos visuales se han reorganizado con la finalidad de que quepan los nuevos componentes asociados al segundo jugador.

Imaginemos que la pantalla es dividida en 4 cuadrantes del mismo tamaño, pero siempre reservando un espacio inferior para las opciones del juego. 

En la sección superior izquierda se encuentran las cartas del dealer y su score.
En la sección superior derecha el cuadrante presenta un espacio reservado para cuando el juego soporte 3 participantes.

En la sección inferior izquierda se encuentran las cartas del jugador 1, su score actual, su balance y apuesta actual.
En la sección inferior derecha se encuentran las cartas del jugador 2, su score actual, su balance y apuesta actual.

Cambios en la UI
---
La descripción anterior permite tener una idea general de los cambios realizados. A continuación se detalla un poco en términos de Android:

1. La vista se representaba mediante posiciones relativas usando un RelativeLayout. Ahora se usarán LinearLayouts con su correspondente orientación vertical u horizontal para ubicar los elementos.
2. La vista presentaba LinearLayouts con 1 único elemento hijo. Estos contenedores se han reemplazado por FrameLayouts por ser más idóneos como contenedores de 1 único elemento.

Por último las opciones ubicadas en la parte inferior, van mostrando el valor que se pretende apostar, y permiten finalmente realizar una apuesta.
Luego que se han fijado las apuestas, los valores correspondientes se muestran en cada cuadrante, con relación a cada participante.

Flujo del juego para 1 solo participante (offline)
---
1. El participante determina la cantidad a apostar y presiona el botón DEAL.
2. Las cartas son repartidas al dealer y al único participante en un instante. Por tanto el participante puede ver la suma de las cartas que tiene.
3. El participante debe usar HIT (para robar una carta adicional) o STAND (para finalizar su turno). Solo si el participante obtuviese el valor de 21 desde el inicio, gana automáticamente sin hacer nada más (BLACKJACK).
3.1. Si el participante decide robar una carta y su suma excede 21, pierde por quiebre (BUSTED).
3.2. Si el participante decide finalizar su turno, se muestra la suma del DEALER y según ello se determina el deselnace (el dealer puede perder por quiebre, puede ocurrir un empate o DRAW, o bien ganar quien tenga el valor más cercano a 21 o 21).
4. El participante repetirá su apuesta dando inicio a un segundo round, y podrá hacer esto mientras disponga de un balance mayor a 0. Por ahora el juego soporta apuestas que siempre son múltiplos de 10.

Flujo del juego para 2 participantes (online) (flujo simplificado)
---
Para esta **primera versión** no se van a considerar varios detalles, a fin de desarrollar una primera funcionalidad básica.
La idea es que 2 jugadores puedan compartir la misma mesa y jueguen contra el dealer, con sus apuestas y balances correspondientes.

1. Los jugadores definen sus apuestas.
Aquí los jugadores determinan con sus opciones la cantidad que van a apostar y presionan el botón DEAL.
Cuando el primer jugador presione DEAL la cantidad que ha apostado se mostrará en pantalla, y también lo verá el otro jugador.
Este primer paso finaliza en el instante que ambos han definido sus apuestas. 
**De momento, no existe un tiempo límite** para decidir si apostar o no, por lo que si un jugador no reacciona el juego no avanzará a un 2do paso, ni el jugador será expulsado (estas medidas se implementarán luego).

2. Mostar cartas.
En este instante, las cartas que obtuvieron los jugadores son mostradas, y cada jugador puede ver las cartas que tiene además de las del otro participante. No hay ningún problema porque ambos están jugando contra el dealer de forma independiente.

 - Si uno de los jugadores (podría aplicar incluso para ambos) ha obtenido BLACKJACK este paso a terminado para él. Cuando el paso finalice para ambos, recién se podrá ir a un tercer paso. Al igual que el caso anterior, no hay tiempo límite para actuar.
 - Cada jugador debe decidir entre las opciones HIT y STAND. La opción STAND finaliza este segundo paso para el jugador que ha decidido usar STAND. La opción de HIT finaliza el turno si el jugador excede 21 o si obtiene 21; de caso contrario, se puede usar HIT o STAND repitiendo lo antes mencionado.
 - El jugador que finalice su turno primero verá sus resultados de forma inmediata, pero no podrá continuar hasta que el otro jugador finalice.

3. Resultados.
Cuando finalice la acción de cada jugador según el punto anterior, se mostrarán los resultados para ambos jugadores.
Es decir, ambos verán los resultados del jugador 1 y del jugador 2.
En este punto, se solicitará que realicen click sobre el mensaje que muestra los resultados, esto confirmará que están listos para una segunda ronda, que inicia por volver a definir una apuesta.
Cuando ambos hayan confirmado se repetirá lo descrito en (1). Sin embargo, si uno de ellos llega a tener un balance de 0, el juego terminaría para ambos. Esto es para evitar definir por ahora una lógica que permita a uno jugar y al otro solo observar.

* Por ahora, la lógica que soporta este flujo es la entidad GameOnline, que se ha basado en la entidad Game, pero que soporta 2 jugadores y las consideraciones para jugar de forma online. Los jugadores por ahora son atributos de tipo Player. Se espera en versiones posteriores, reemplazar estos 2 objetos por un ArrayList que soporte una mayor cantidad de participantes en simultáneo.

Broadcast
---
El juego ButtonClicker2000 (que Google ha publicado a modo de ejemplo) consiste en que cada jugador hace broadcast (emite mensajes) a los demás jugadores, informando por cada click que ha realizado.

Estos mensajes se emiten a través de un buffer (determinado por un arreglo de bytes), y en el caso del juego mencionado existen 2 tipos de mensajes. 
Un primer tipo comunica a los demás jugadores el valor del score del jugador que emite el mensaje, y esto ocurre cada vez que se hace click durante los 20 segundos que dura el juego.
Un segundo tipo, también comunica el score del jugador, pero es un score que se emite solo una vez al final del juego, cunado el tiempo se ha agotado.
En este caso, el buffer consta de 2 bytes, el primero indica el tipo de mensaje a través de una letra y el segundo byte es el score del jugador.

En el caso de 21Barth, debemos manejar más tipos de mensajes. Estos mensajes comunicarán al otro jugador la acción que cada uno de ellos realiza. Así tenemos:

- Definir apuesta. Indica que el jugador ha presionado el botón de DEAL luego de definir el valor de su apuesta. En este caso el array de bytes constará de 2 bytes. El primer byte será la letra "D" y el segundo byte contendrá el valor que se ha decidido apostar.

- Solicitar carta. Indica que el jugador ha presionado el botón HIT. En este caso el host va a generar una carta y hará broadcast de este valor al jugador que presionó HIT. Solicitar 1 carta se representa por 2 bytes: el caracter H y el número 0.
Mientras que asignar una carta se representa por 3 bytes: el caracter H, un caracter que representa un suite (HEARTS, DIAMONDS, CLUBS, SPADES) y un value (representado por un caracter, por ejemplo "A", "Q", "1").

- Finalizar jugada. Indica que el jugador ha presionado el botón STAND. En este caso el buffer a comunicar consta solo de 1 caracter, determinado por la letra S.

- Repartir mano inicial. Este mensaje lo emite el host de la partida y se usa para comunicar las 2 primeras cartas al jugador que no es host. El formato del mensaje es I, seguido de un suite y un value correspondientes a la primera carta, y luego nuevamente un suite y value (segunda carta).
