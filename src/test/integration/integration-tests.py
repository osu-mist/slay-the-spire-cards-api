import auxillaries
import json
import unittest
import sys

class TestStringMethods(unittest.TestCase):

    # Test POST, GET, PUT, DELETE
    def testCombined(self):
        # Test postCard with valid body
        validPost = auxillaries.postCard(config["validPostBody"], url, user, passw)
        self.assertEqual(validPost.status_code, 201)
        self.assertIsNotNone(validPost.json()["data"])
        self.assertEqual(validPost.json()["data"]["type"], "card")

        validId = validPost.json()["data"]["id"]

        # Test getCardById with valid ID
        validGet = auxillaries.getCardById(validId, url, user, passw)
        self.assertEqual(validGet.status_code, 200)
        self.assertIsNotNone(validGet.json()["data"])
        self.assertEqual(validGet.json()["data"]["type"], "card")

        # Test putCard with valid ID and body
        validPut = auxillaries.putCard(validId, config["validPostBody"], url, user, passw)
        self.assertEqual(validPut.status_code, 200)
        self.assertIsNotNone(validPut.json()["data"])
        self.assertEqual(validPut.json()["data"]["type"], "card")

        # Test deleteCard with valid ID
        validDelete = auxillaries.deleteCard(validId, url, user, passw)
        self.assertEqual(validDelete.status_code, 204)

    def testGetCardById(self):
        # Invalid card ID
        invalidId = auxillaries.getCardById(config["invalidCardId"], url, user, passw)
        self.assertEqual(invalidId.status_code, 404)

    def testGetCards(self):
        # Valid parameters
        validParams = auxillaries.getCards(config["validGetParams"], url, user, passw)
        self.assertEqual(validParams.status_code, 200)
        self.assertIsNotNone(validParams.json()["data"])
        self.assertEqual(validParams.json()["data"][0]["type"], "card")

        ### Invalid parameters

        # Invalid type
        testInvalidGetAttribute("types", self, "skill")

        # Invalid color
        testInvalidGetAttribute("colors", self, "red")

        # Invalid rarity
        testInvalidGetAttribute("rarities", self, "basic")        

        # Invalid name
        testInvalidGetAttribute("name", self, "Defend")

        # Invalid keywords
        testInvalidGetAttribute("keywords", self, "block")

        # energyMin out of range
        config["validGetParams"]["energyMin"] = -1
        minOutOfRange = auxillaries.getCards(config["validGetParams"], url, user, passw)
        self.assertEqual(minOutOfRange.status_code, 400)
        self.assertIn("energyMin or energyMax out of range",
                      minOutOfRange.json()["developerMessage"])
        config["validGetParams"]["energyMin"] = 0

        # energyMax out of range
        config["validGetParams"]["energyMax"] = 1000
        maxOutOfRange = auxillaries.getCards(config["validGetParams"], url, user, passw)
        self.assertEqual(maxOutOfRange.status_code, 400)
        self.assertIn("energyMin or energyMax out of range",
                      maxOutOfRange.json()["developerMessage"])
        config["validGetParams"]["energyMax"] = 0


    def testPostCard(self):
        ### Invalid body

        # Invalid type
        testInvalidAttribute("type", self, "skill", False)

        # Invalid color
        testInvalidAttribute("color", self, "red", False)

        # Invalid rarity
        testInvalidAttribute("rarity", self, "basic", False)

        # Invalid name
        testInvalidAttribute("name", self, "Defend", False)

        # Invalid description
        testInvalidAttribute("description", self, "Gain 5 block.", False)

        # Invalid energy
        testInvalidAttribute("energy", self, 1, False)

    def testPutCard(self):
        # Invalid ID
        invalidId = auxillaries.putCard(config["invalidCardId"],
                                        config["validPostBody"],
                                        url, user, passw)
        self.assertEqual(invalidId.status_code, 404)

        ### Invalid body

        # Invalid type
        testInvalidAttribute("type", self, "skill", True)

        # Invalid color
        testInvalidAttribute("color", self, "red", True)

        # Invalid rarity
        testInvalidAttribute("rarity", self, "basic", True)

        # Invalid name
        testInvalidAttribute("name", self, "Defend", True)

        # Invalid description
        testInvalidAttribute("description", self, "Gain 5 block.", True)

        # Invalid energy
        testInvalidAttribute("energy", self, 1, True)

# Test invalid attribute in PUT or POST body
def testInvalidAttribute(attribute, self, defaultValue, isPut):
    config["validPostBody"]["data"]["attributes"][attribute] = "bad" + attribute + "()"
    if isPut:
        invalidAttribute = auxillaries.putCard(config["validCardId"],
                                               config["validPostBody"],
                                               url, user, passw)
    else:
        invalidAttribute = auxillaries.postCard(config["validPostBody"], url, user, passw)
    self.assertEqual(invalidAttribute.status_code, 400)
    self.assertIn("Invalid " + attribute, invalidAttribute.json()["developerMessage"])
    config["validPostBody"]["data"]["attributes"][attribute] = defaultValue

# Test invalid parameter in getCards
def testInvalidGetAttribute(attribute, self, defaultValue):
    config["validGetParams"][attribute] = "bad" + attribute + "()"
    invalidGetAttribute = auxillaries.getCards(config["validGetParams"], url, user, passw)
    self.assertEqual(invalidGetAttribute.status_code, 400)
    self.assertIn("Invalid " + attribute, invalidGetAttribute.json()["developerMessage"])
    config["validGetParams"][attribute] = defaultValue

if __name__ == '__main__':
    namespace, args = auxillaries.parse_args()
    config = json.load(open(namespace.inputfile))
    url = config["hostname"] + config["version"] + config["api"]
    user = config["username"]
    passw = config["password"]

    sys.argv = args
    unittest.main()
