# sunflower
Sunflower is a tool for extracting and representing category graph of words. 
The key idea is using different versions (languages) of Wikipedia to generate the category graph of the input.

Instructions

1. To run Sunflower as a stand-alone web application:
You need:
Tomcat (or other appropriate) server/servlet container.
http://tomcat.apache.org/
Maven 
https://maven.apache.org/

	Step 1. Start tomcat
	Step 2. Deploy the sunflower.war (located in sunflower/target folder). You should see a folder named sunflower is built in the webapp folder of tomcat after deploying.
	Step 3. Go to localhost:8080/sunflower using your browser
	If the sunflower application is not working at this stage, please check the log file of your tomcat and make sure the application is deployed successfully. 


2. To view a demo of different visualization components and web services please visit: ws.cs.dal.ca:8080/sunflower
    Visualization of the graph:
    http://ws.cs.dal.ca:8080/sunflower/
    Visualization of tag profile derived from the graph (simple linear combination of scores)
    http://ws.cs.dal.ca:8080/sunflower/tagprofile/
    The graph in json (replace Dalhousie_University with any Wikipedia id of a Wikipedia article):
    http://ws.cs.dal.ca:8080/sunflower/concept/paths/Dalhousie_University?width=3&depth=4&noPruning=true&fullLabels=true
    And the profile:
    http://ws.cs.dal.ca:8080/sunflower/concept/profile?id=Dalhousie_University&width=3&depth=4&noPruning=true&fullLabels=true

If you use Sunflower in your research, kindly reference the following paper, which describes an ERD system built on Sunflower, and includes a description of Sunflower.
@inproceedings{Lipczak:2014:TLE:2633211.2634351,
 author = {Lipczak, Marek and Koushkestani, Arash and Milios, Evangelos},
 title = {Tulip: Lightweight Entity Recognition and Disambiguation Using Wikipedia-based Topic Centroids},
 booktitle = {Proceedings of the First International Workshop on Entity Recognition \&\#38; Disambiguation},
 series = {ERD '14},
 year = {2014},
 isbn = {978-1-4503-3023-7},
 location = {Gold Coast, Queensland, Australia},
 pages = {31--36},
 numpages = {6},
 url = {http://doi.acm.org/10.1145/2633211.2634351},
 doi = {10.1145/2633211.2634351},
 acmid = {2634351},
 publisher = {ACM},
 address = {New York, NY, USA},
 keywords = {entity recognition and disambiguation, erd 2014 - long track, linked open data, term centroids, text annotation, wikification},
} 
