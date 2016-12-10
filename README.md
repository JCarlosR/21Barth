Versi�n inicial (offline) (1 jugador y dealer)
---
La pantalla se encontraba repartida como lo muestra la imagen siguiente:

En este caso, en la parte superior izquierda se encuentra el dealer y su score representado por un signo de interrogaci�n.

Debajo de las cartas del dealer se encuentra la apuesta realizada por el jugador. 
El jugador ganar� o perder� esa cantidad.

Centrado verticalmente hacia la derecha se encuentra el balance del jugador. 
Si llega a 0, pierde.

En el lado inferior izquierdo se encuentran las cartas del jugador.
Y un poco m�s a la derecha, su score actual.

Finalmente, en la parte inferior se encuentran las opciones del juego.


Versi�n realtime (online) (2 jugadores y dealer)
---
Los elementos visuales se han reorganizado con la finalidad de que quepan los nuevos componentes asociados al segundo jugador.

Imaginemos que la pantalla es dividida en 4 cuadrantes del mismo tama�o, pero siempre reservando un espacio inferior para las opciones del juego. 

En la secci�n superior izquierda se encuentran las cartas del dealer y su score.
En la secci�n superior derecha se encuentran los balances de ambos jugadores, y debajo las apuestas que cada uno realiz�.

En la secci�n inferior izquierda se encuentran las cartas del jugador 1 y su score actual.
En la secci�n inferior derecha se encuentran las cartas del jugador 2 y su score actual.

Cambios en la UI
---
La descripci�n anterior permite tener una idea general de los cambios realizados. A continuaci�n se detalla un poco en t�rminos de Android:

1. La vista se representa mediante posiciones relativas en un RelativeLayout. Ahora se usar�n LinearLayouts con orientaciones vertical y horizontal para ubicar los elementos.
2. La vista presentaba LinearLayouts con 1 �nico elemento hijo. Estos contenedores se han reemplazado por FrameLayouts por ser m�s id�neos como contenedores de 1 �nico elemento.

Por �ltimo las opciones del juego permiten realizar acciones, y van mostrando el valor que se desea apostar.
Luego que se ha fijado la apuesta, las apuestas ubicadas en el cuadrante superior derecho se actualizan y se da inicio al juego.