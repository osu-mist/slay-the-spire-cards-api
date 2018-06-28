import auxillaries
import local_vars
import json
import unittest
import sys


class TestStringMethods(unittest.TestCase):

    # Test POST, GET, PUT, DELETE
    def test_combined(self):
        # Test post card with valid body
        valid_post = auxillaries.post_card(config["validPostBody"])
        test_valid_response(self, valid_post, 201)

        valid_id = valid_post.json()["data"]["id"]

        # Test get card by id with valid ID
        valid_get = auxillaries.get_card_by_id(valid_id)
        test_valid_response(self, valid_get, 200)

        # Test put card with valid ID and body
        valid_put = auxillaries.put_card(valid_id, config["validPostBody"])
        test_valid_response(self, valid_put, 200)

        # Test delete card with valid ID
        valid_delete = auxillaries.delete_card(valid_id)
        self.assertEqual(valid_delete.status_code, 204)

    def test_get_card_by_id(self):
        # Invalid card ID
        invalid_id = auxillaries.get_card_by_id(config["invalidCardId"])
        self.assertEqual(invalid_id.status_code, 404)

    def test_get_cards(self):
        # Valid parameters
        valid_params = auxillaries.get_cards(config["validGetParams"],
                                             )
        self.assertEqual(valid_params.status_code, 200)
        self.assertIsNotNone(valid_params.json()["data"])
        self.assertEqual(valid_params.json()["data"][0]["type"], "card")

        # Invalid parameters

        # Invalid type
        test_invalid_get_attribute(self, "types", "skill")

        # Invalid color
        test_invalid_get_attribute(self, "colors", "red")

        # Invalid rarity
        test_invalid_get_attribute(self, "rarities", "basic")

        # Invalid name
        test_invalid_get_attribute(self, "name", "Defend")

        # Invalid keywords
        test_invalid_get_attribute(self, "keywords", "block")

        # energyMin out of range
        config["validGetParams"]["energyMin"] = -1
        minOutOfRange = auxillaries.get_cards(config["validGetParams"],
                                              )
        self.assertEqual(minOutOfRange.status_code, 400)
        self.assertIn("energyMin or energyMax out of range",
                      minOutOfRange.json()["developerMessage"])
        config["validGetParams"]["energyMin"] = 0

        # energyMax out of range
        config["validGetParams"]["energyMax"] = 1000
        maxOutOfRange = auxillaries.get_cards(config["validGetParams"],
                                              )
        self.assertEqual(maxOutOfRange.status_code, 400)
        self.assertIn("energyMin or energyMax out of range",
                      maxOutOfRange.json()["developerMessage"])
        config["validGetParams"]["energyMax"] = 0

    def test_post_card(self):
        # Invalid body

        # Invalid type
        test_invalid_attribute(self, "type", "skill", False)

        # Invalid color
        test_invalid_attribute(self, "color", "red", False)

        # Invalid rarity
        test_invalid_attribute(self, "rarity", "basic", False)

        # Invalid name
        test_invalid_attribute(self, "name", "Defend", False)

        # Invalid description
        test_invalid_attribute(self, "description", "Gain 5 block.", False)

        # Invalid energy
        test_invalid_attribute(self, "energy", 1, False)

    def test_put_card(self):
        # Invalid ID
        invalid_id = auxillaries.put_card(config["invalidCardId"],
                                          config["validPostBody"])
        self.assertEqual(invalid_id.status_code, 404)

        # Invalid body

        # Invalid type
        test_invalid_attribute(self, "type", "skill", True)

        # Invalid color
        test_invalid_attribute(self, "color", "red", True)

        # Invalid rarity
        test_invalid_attribute(self, "rarity", "basic", True)

        # Invalid name
        test_invalid_attribute(self, "name", "Defend", True)

        # Invalid description
        test_invalid_attribute(self, "description", "Gain 5 block.", True)

        # Invalid energy
        test_invalid_attribute(self, "energy", 1, True)

    # def test_delete_card:
        # Invalid id


# Test valid response in combined test
def test_valid_response(self, response, code):
    self.assertEqual(response.status_code, code)
    self.assertIsNotNone(response.json()["data"])
    self.assertEqual(response.json()["data"]["type"], "card")


# Test invalid attribute in PUT or POST body
def test_invalid_attribute(self, attribute, defaultValue, isPut):
    config["validPostBody"]["data"]["attributes"][attribute] = (
        "bad" + attribute + "()"
    )
    if isPut:
        invalidAttribute = auxillaries.put_card(config["validCardId"],
                                                config["validPostBody"])
    else:
        invalidAttribute = auxillaries.post_card(config["validPostBody"])
    self.assertEqual(invalidAttribute.status_code, 400)
    self.assertIn("Invalid " + attribute,
                  invalidAttribute.json()["developerMessage"])
    config["validPostBody"]["data"]["attributes"][attribute] = defaultValue


# Test invalid parameter in get_cards
def test_invalid_get_attribute(self, attribute, defaultValue):
    config["validGetParams"][attribute] = "bad" + attribute + "()"
    invalid_get_attribute = auxillaries.get_cards(config["validGetParams"],
                                                  )
    self.assertEqual(invalid_get_attribute.status_code, 400)
    self.assertIn("Invalid " + attribute,
                  invalid_get_attribute.json()["developerMessage"])
    config["validGetParams"][attribute] = defaultValue

if __name__ == '__main__':
    namespace, args = auxillaries.parse_args()
    config = json.load(open(namespace.inputfile))
    local_vars.init(config)
    url = config["hostname"] + config["version"] + config["api"]
    user = config["username"]
    passw = config["password"]

    sys.argv = args
    unittest.main()
