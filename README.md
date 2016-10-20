# SEE-HLA-Starter-Kit

The development of a HLA Federation with its Federates is a quite complex task and there are few training resources for developers [1,2, 4]. The SEE HLA Starter Kit aims to ease the development of HLA federates in the context of the Simulation Exploration Experience (SEE) project by providing the following resources:

- a *software framework (the SKF)* for the development in Java of SEE Federates;
- a *technical documentation* that describes the SKF;
- a *user guide* to support developers in the use of the SKF;
- a set of *reference examples* of SEE Federates created by using the SKF;
- *video-tutorials*, which show how to create both the structure and the behavior of a SEE Federate by using the SKF.
The SKF is released under the open source policy Lesser GNU Public License (LGPL), which facilitates the development of HLA Federates. Indeed, the SEE HLA Starter Kit allows developers to focus on the specific aspects of their own HLA federates rather than dealing with the common HLA issues such as the management of the simulation time, the connection on the RTI, etc. Moreover, the SEE HLA Starter Kit supports the implementation of SEE Dummy and Tester Federate so to allow a more accurate and effective testing. These features should improve the reliability of SEE Federates and thus reduce the problems arising during the final integration and testing phases of the SEE project [4, 5].
The SKF is fully implemented in the Java language and is based on the following three principles [3]: 
	
-  *Interoperability*, SKF is fully compliant with the IEEE 1516-2010 specifications; as a consequence, it is platform-independent and can interoperate with different HLA RTI implementations (e.g. PITCH, VT/MÄK, PoRTIco, CERTI);
-  *Portability and Uniformity*, SKF provides a homogeneous set of APIs that are independent from the underlying HLA RTI and Java version. In this way, developers could decide the HLA RTI and the Java run-time environment at development-time;
-  *Usability*, the complexity of the features provided by the DKF framework are hidden behind an intuitive set of APIs.

The SEE HLA Starter Kit is designed, developed, released and managed by the SEI (Systems Engineering and Integration) team, operating in the System Modeling and Simulation Hub (SMASH) Lab of the Department of Informatics, Modeling, Electronics and Systems Engineering (DIMES), University of Calabria (Italy), working in cooperation with NASA JSC (Johnson Space Center), Houston (TX, USA).

## **External links**

- The SEE HLA Starter Kit all-in-one package [link](https://drive.google.com/drive/folders/0B5VINCL02C8rQ0xZYmhaTjFfRGc?usp=sharing);
- Technical documentations [link](https://drive.google.com/file/d/0B5VINCL02C8rdktZSGZSdXMyQUE/view?usp=sharing);
- Examples and video-tutorials: How to build a Federate from scratch in 30 minutes! [link](https://drive.google.com/open?id=0B6Txsul1iIJma3pITXE1M2hSOVk);

## **Working team**

*  Alfredo Garro, [alfredo.garro@unical.it](mailto:alfredo.garro@unical.it) (coordinator);
*  Alberto Falcone, [alberto.falcone@dimes.unical.it](mailto:alberto.falcone@dimes.unical.it) (main developer); 
*  Andrea Tundis, [andrea.tundis@dimes.unical.it](mailto:andrea.tundis@dimes.unical.it) (developer).

## **Acknowledgments**

The SKF working team would like to thank Edwin Z. Crues (NASA JCS) for his precious advice and suggestions in the development of the SEE HLA Starter Kit. A special note of thanks goes also to all the NASA staff involved in the Simulation Exploration Experience (SEE) Project: Priscilla Elfrey, Stephen Paglialonga, Michael Conroy, Dan Dexter, Daniel Oneil, to Björn Möller (PITCH Technologies), and to all the members of SEE teams.

<br>
 
## **References**

1.  Falcone, A., Garro, A., Taylor, S. J. E., Anagnostou, A., Chaudhry, N. R., Salah, O. *Experiences in simplifying distributed simulation: The HLA development kit framework*. Journal of Simulation, 10(37), 1–20. http://doi.org/10.1057/s41273-016-0039-4, (2016).

2. Anagnostou, A., Chaudhry, N.R., Falcone, A., Garro, A., Salah, O., Taylor, S.J.E., *Easing the development of HLA Federates: the HLA Development Kit and its exploitation in the SEE Project*. In Proc. of the 19th IEEE/ACM International Symposium on Distributed Simulation and Real Time Applications (ACM/IEEE DS-RT), Chengdu, China, October, 14-16, IEEE Computer Society, (2015).

3. Anagnostou, A., Chaudhry, N.R., Falcone, A., Garro, A., Salah, O., Taylor, S.J.E., *A Prototype HLA Development Kit: Results from the 2015 Simulation Exploration Experience*. In Proc. of the ACM SIGSIM PADS 2015, London, UK, June, 10-12, (2015).

4. Bocciarelli, P., D’Ambrogio, A., Falcone, A., Garro, A., Giglio, A., *A model-driven approach to enable the distributed simulation of complex systems*. In Proc. of the 6th Complex Systems Design & Management (CSD&M) 2015, Paris, France, November 23-25, (2015).

5. Falcone, A., Garro, A., Longo, F., Spadafora, F., *SimulationExploration Experience: A Communication System and a 3D Real Time Visualization for a Moon base simulated scenario*. In Proc. of the 18th IEEE/ACM International Symposium on Distributed Simulation and Real Time Applications (ACM/IEEE DS-RT), Toulouse, France, October, 1-3, IEEE Computer Society, (2014).