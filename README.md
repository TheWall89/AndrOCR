#AndrOCR
* * *

AndrOCR is an Android Application for Optical character Recognition using Google's Tesseract 3.01 engine.

This is not real good coding, but I'd like to share my code, because lots of people asked me about it.

Application is also available on the Play Store (free, no ads) at the following link: https://play.google.com/store/apps/details?id=com.matpergo.androcr

Compile Notes
-
To compile AndrOCR you need to import it in Eclipse. To compile, you need also to compile and import tess-two project.

### Dependencies
I'm using an old version of tess-two, because newer ones causes problems with some images 

To compile tess-two, do the following

		git clone git://github.com/rmtheis/tess-two tess
		cd tess
		git checkout 071820a324		
    	cd tess-two
    	ndk-build
    	android update project --path .
    	ant release

Now, import the compiled tess-two into Eclipse and add it to AndrOCR as a Library.

How to contribute
-
I don't have much time to work on this project. If you like to help me, you're welcome.


License
-
This project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

		/*
		* Copyright 2013 Matteo Pergolesi (matpergo@gmail.com)
		*
		* Licensed under the Apache License, Version 2.0 (the "License");
 		* you may not use this file except in compliance with the License.
 		* You may obtain a copy of the License at
 		*
		*      http://www.apache.org/licenses/LICENSE-2.0
		*
		* Unless required by applicable law or agreed to in writing, software
		* distributed under the License is distributed on an "AS IS" BASIS,
		* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
		* See the License for the specific language governing permissions and
		* limitations under the License.
		*/

Credits
-

This product includes open source software released under the Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html).

tess-two library is open source software written by Robert Theis. The source code is available from https://github.com/rmtheis/tess-two

tesseract-ocr is open source software written by Ray Smith and others. The official project page is http://code.google.com/p/tesseract-ocr/

microsoft-translator-java-api is open source software written by Jonathan Griggs. The source code is available from http://code.google.com/p/microsoft-translator-java-api/

The application icon is derived from http://it.wikipedia.org/wiki/File:Hypercube.svg

Logo Font: Droid Logo by ei8htohms		